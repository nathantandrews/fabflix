package dev.wdal.main;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class JdbcFinalizer implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
    }
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            // Loop through all drivers
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getClassLoader() == cl) {
                    // This driver was registered by the webapp's ClassLoader, so deregister it:
                    System.out.println("Deregistering JDBC driver {}" + driver);
                    DriverManager.deregisterDriver(driver);
                } else {
                    // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                    System.out.println("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader" + driver);
                }
            }
        }
        catch (Exception ex) {
            System.out.println("Could not deregister JDBC driver:".concat(ex.getMessage()));
        } 
    }
}
