# CUCM_Jabber2Line_Conversion

Sample code for converting CUCM Phone from single line to dual line.

## Introduction:

This sample Java code demonstrates how CUCM AXL API can be used to covert the current single line phones on CUCM to dual line.
This piece of code pushes the current on users phone to line2 and configures line1 with new provided contact center extension.
*line = Extension (interchangeably used)

## Use Case:

During an emergency situation line COVID-19, users can be provided additional Contact center line on their current softphone (Jabber or CIPC) which users can then use to login to contact center and take calls from the queue.

## Requirements:

You will need Java JRE 1.8 or higher installed on your computer.
OPTIONAL: You will need JDK 1.8 or higher installed (and latest version of eclipse) on your computer to explore the code base.
OPTIONAL: Maven for build and dependency management.

## Limitation:

The sample code only works with devices which has recording capability, but can be modified to work with other types of devices.

## Pre-reun configuration

1. Make sure 11.6 or later CUCM server is installed
2. Make sure AXL service on CUCM host is enabled
3. Add a user on CUCM as enduser and provide Administration access to the Callmanager AXL API.
5. Make sure the phones you are trying to migrate are configured on CUCM with single line.
4. For testing make sure there is a phone configured with least basic configuration as below:

![base phone](https://raw.githubusercontent.com/chintangajjar013/cucm_Jabber2Line/master/prerun.png)

## Post-run phone configuration

After running the bat file with below input sample phone looks like below:

CHGAJJARGDPR,NULL,80940000,80940001,NULL,NULL,NULL,N,N,NULL,NULL,NULL,NULL,NULL

![base phone](https://raw.githubusercontent.com/chintangajjar013/cucm_Jabber2Line/master/postrun.png)

Line1 is pushed to Line2 and Line1 now is configured with new line.

## How to Use:

while the purpose of this repo is to demonstrate the usability of AXL API, but if you wish to use the already compiled jar file which does the line switching operation below is procedure.

1. Install JRE 1.8 or Higher and configure Class Path.
2. Create a folder, and copy the **Runner.jar**, **Input.csv**, **config.properties** and **run_me.bat** file.
3. Modify config.properties to make sure you have correct AXL API user/password and CUCM host address set here. 
4. The Code uses HTTPS connection to CUCM so if you are using self signed certs you will have to import those certs in to the JRE trust store, google for additional details on this.
5. Modify **Input.csv** file
The content of Input CSV should be as below

<DeviceName>,<CurrentLine>,<NewLine>,<Partition>,<ExtMask>,<MaxCalls>,<BusyTrigger>,<Line1Record>,<Line2Record>,<RecordingProfile>,<RecordingFlag>,<RecordingSource>,<MonitorCSS>,<user>

DeviceName = Name of the Device which needs to be converted to dual line (Mandatory param)

CurrentLine = Current Line which will be pushed to Line2 from Line1 (Mandatory param)

NewLine= New Line which will be configured on Line1 for the device (Mandatory param)

Partition= CUCM Route Partition, we consider here that both Line1 and Line2 uses same partition. (Mandatory Param, provide NULL if not not applicable)

ExtMask=External Mask to be set on Line1 and Line2 (Mandatory Param, provide NULL if not not applicable)

MaxCalls= Max calls to be set on line (Mandatory Param, provide NULL if not not applicable defaults to 2)

BusyTrigger= BusyTrigger to be set on line (Mandatory Param, provide NULL if not not applicable defaults to 1)

Line1Record= Y if your want to configure recording on the line 1, N if not (mandatory param, default N)

Line2Record= Y if your want to configure recording on the line 2, N if not (mandatory param, default N)

RecordingProfile= name of the recording profile to be used if selected Y for the recording (mandatory param, provide NULL if not applicable)

RecordingFlag= recording type to be used if selected Y for the recording (mandatory param, provide NULL if not applicable)

RecordingSource= recording source to be used if selected Y for the recording (mandatory param, provide NULL if not applicable)

MonitorCSS = Monitoring CSS name to be configured (mandatory param, provide NULL if not applicable)

user= userid of the end user to be associated to the line, user must 
exist on CUCM as an end user (mandatory param, provide NULL if not applicable)
 
i.e. 

**SEP12312313,CORE 8Digit,85748070,80893699,199999999,2,1,Y,Y,Zoomint Recording Profile,Automatic Call Recording Enabled,Phone Preferred,NULL,bobsmith**

**CS213123213,CORE 8Digit,85748071,80980502,199999999,NULL,NULL,Y,N,NULL,NULL,Phone Preferred,NULL,NULL**
  
6. execute run_me.bat, you can either monitor console and look at the log-file generated locally to evaluate the results.

## Additional notes:

-> Do not modify the name of the files, the code looks for exact file name as shipped with sample project.

-> Make sure to run the code with one or two phone samples and then doing it for batch devices.

-> Modify the code to meet your business specific needs.

-> Make sure AXL API is enabled on CUCM node provided in config. properties file.

-> Make sure AXL user has been created on CUCM and has required privileges.


## License

This project is licensed to you under the terms of the [Cisco Sample Code License](https://github.com/chintangajjar013/cucm_Jabber2Line/blob/master/LICENSE)
