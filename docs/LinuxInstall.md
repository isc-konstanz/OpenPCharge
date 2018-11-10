![emonmuc header](img/emonmuc-logo.png)

This document describes how to install the  (**e**nergy **mon**itoring **m**ulty **u**tility **c**ommunication), an open-source protocoll driver project to enable the communication with a variety of metering or other devices, developed based on the [OpenMUC](https://www.openmuc.org/) project.


---------------

# 1 Install OpenPCharge

This short documentation will assume the generic **version 1.0.0** of the driver as a simplification.
To install the OSGi bundle, simply download the latest release tarball and move the bundle into the emonmuc frameworks *bundles* directory

~~~shell
wget --quiet --show-progress https://github.com/isc-konstanz/OpenPCharge/releases/download/v1.0.0/OpenPCharge-1.0.0.tar.gz
tar -xzf OpenPCharge-1.0.0.tar.gz
mv ./OpenPCharge/libs/openmuc-app-pcharge-1.0.0.jar /opt/emonmuc/bundles/
mv ./OpenPCharge/libs/openmuc-driver-pcharge-1.0.0.jar /opt/emonmuc/bundles/
~~~

Afterwards restart the framework, for the driver to be started

~~~
emonmuc restart
~~~


## 1.2 Configuration

For the application to know the correct channels to be used, a configuration file needs to be created or the provided default file used.

~~~
mv ./OpenPCharge/conf/p-charge.conf /opt/emonmuc/conf/p-charge.conf
nano /opt/emonmuc/conf/p-charge.conf
~~~

Adjust the channel IDs accordingly:

>     [Port1]
>         # Minimum amount of minutes, after which the 
>         # charge process may be started again [min]
>         startIntervalMin = <minutes>
>     
>         # IDs of registered P-CHARGE channels
>         portStatus = <channelId>
>         completeStatus = <channelId>
>         authorizationStatus = <channelId>
>         currentLimit = <channelId>
>         event = <channelId>


## 1.2 Device templates

Next, device template files are provided by this project, to ease up the configuration of some new hardware devices.  
Those can be found at *libs/device/pcharge* and should be moved to the corresponding directory in the emonmuc root:

~~~shell
mv ./OpenPCharge/libs/device/pcharge /opt/emonmuc/lib/device/
~~~


## 1.3 Finish

At last, don't forget to remove the released tarball to avoid cluttering of your system.

~~~
rm -rf ./OpenPCharge*
~~~