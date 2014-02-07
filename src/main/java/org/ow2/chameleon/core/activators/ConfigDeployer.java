package org.ow2.chameleon.core.activators;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.services.ExtensionBasedDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Bundle deployer.
 */
public class ConfigDeployer extends ExtensionBasedDeployer implements BundleActivator, ServiceListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigDeployer.class);

    Map<File, Configuration> configurations = new HashMap<File, Configuration>();
    private BundleContext context;

    public ConfigDeployer() {
        super("cfg");
    }

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        context.registerService(Deployer.class, this, null);
        context.addServiceListener(this, "(" + Constants.OBJECTCLASS + "=" + ConfigurationAdmin.class.getName() + ")");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        removeAllConfigurations();
    }

    private Properties read(File file) throws IOException {
        InputStream in = null;
        try {
            Properties p = new Properties();
            in = new FileInputStream(file);
            p.load(in);
            return p;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Parses cfg file associated PID. This supports both ManagedService PID and
     * ManagedServiceFactory PID
     *
     * @param path the path
     * @return structure {pid, factory pid} or {pid, <code>null</code> if not a
     * Factory configuration.
     */
    String[] parsePid(String path) {
        String pid = path.substring(0, path.length() - ".cfg".length());
        int n = pid.indexOf('-');
        if (n > 0) {
            String factoryPid = pid.substring(n + 1);
            pid = pid.substring(0, n);
            return new String[]{pid, factoryPid};
        } else {
            return new String[]{pid, null};
        }
    }

    private void readAndApplyConfiguration(File file, ConfigurationAdmin admin) throws Exception {
        synchronized (this) {
            if (admin == null) {
                LOGGER.warn("Cannot apply configuration " + file.getName() + " - no configuration admin");
                configurations.put(file, UnmanagedConfiguration.INSTANCE);
            } else {
                Properties properties = read(file);
                String[] pid = parsePid(file.getName());
                Dictionary<Object, Object> ht = new Properties();
                for (String k : properties.stringPropertyNames()) {
                    ht.put(k, properties.getProperty(k));
                }
                Configuration config = configurations.get(file);
                if (config == null || config == UnmanagedConfiguration.INSTANCE) {
                    config = getConfiguration(pid[0], pid[1], admin);
                    if (config.getBundleLocation() != null) {
                        config.setBundleLocation(null);
                    }
                }
                LOGGER.info("Updating configuration {} in the configuration admin, configuration: {}",
                        config.getPid(), configurations);
                config.update(ht);

                configurations.put(file, config);
            }
        }
    }

    /**
     * Gets the configuration admin service.
     *
     * @return the Configuration Admin service object, {@literal null} if not found.
     */
    private ConfigurationAdmin getConfigurationAdmin() {
        // Should be there !
        ServiceReference<ConfigurationAdmin> ref = context
                .getServiceReference(ConfigurationAdmin.class);
        if (ref == null) {
            return null;
        } else {
            return context.getService(ref);
        }
    }

    /**
     * Gets a Configuration object.
     *
     * @param pid        the pid
     * @param factoryPid the factory pid
     * @param cm         the config admin service
     * @return the Configuration object (used to update the configuration)
     * @throws IOException if the Configuration object cannot be retrieved
     */
    Configuration getConfiguration(String pid, String factoryPid,
                                   ConfigurationAdmin cm) throws IOException {
        Configuration newConfiguration;
        if (factoryPid != null) {
            newConfiguration = cm.createFactoryConfiguration(pid, "?");
        } else {
            newConfiguration = cm.getConfiguration(pid, "?");
        }
        return newConfiguration;
    }

    @Override
    public void onFileCreate(File file) {
        LOGGER.info("File creation event received for {}", file.getAbsoluteFile());

        synchronized (this) {
            try {
                ConfigurationAdmin admin = getConfigurationAdmin();
                readAndApplyConfiguration(file, admin);
            } catch (Exception e) {
                LOGGER.error("Cannot find the configuration admin service", e);
            }

        }
    }

    @Override
    public void onFileDelete(File file) {
        synchronized (this) {
            Configuration configuration = configurations.remove(file);
            if (! configuration.equals(UnmanagedConfiguration.INSTANCE)) {
                try {
                    LOGGER.info("Deleting configuration {}", configuration.getPid());
                    configuration.delete();
                } catch (Exception e) {
                    LOGGER.error("Cannot delete configuration from {}", configuration.getPid(), e);
                }
            }
        }
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            ConfigurationAdmin admin = (ConfigurationAdmin) context.getService(event.getServiceReference());
            processAllConfigurations(admin);
        } else if (event.getType() == ServiceEvent.UNREGISTERING) {
            removeAllConfigurations();
        }
    }

    private void removeAllConfigurations() {
        synchronized (this) {
            for (Map.Entry<File, Configuration> entry : configurations.entrySet()) {
                if (entry.getValue().equals(UnmanagedConfiguration.INSTANCE)) {
                    try {
                        LOGGER.info("Deleting configuration {}", entry.getValue().getPid());
                        entry.getValue().delete();
                        entry.setValue(UnmanagedConfiguration.INSTANCE);
                    } catch (Exception e) {
                        LOGGER.error("Cannot delete configuration from {}", entry.getKey().getAbsoluteFile(), e);
                    }
                }
            }
        }
    }

    private void processAllConfigurations(ConfigurationAdmin admin) {
        synchronized (this) {
            for (Map.Entry<File, Configuration> entry : configurations.entrySet()) {
                if (entry.getValue().equals(UnmanagedConfiguration.INSTANCE)) {
                    try {
                        readAndApplyConfiguration(entry.getKey(), admin);
                    } catch (Exception e) {
                        LOGGER.error("Cannot apply configuration from {}", entry.getKey().getAbsoluteFile(), e);
                    }
                }
            }
        }
    }

}
