# PicketLink Integration Tests

## Overview

This is an Integration Test Suite for PicketLink. It provides tests covering the following aspects:

* PicketLink Base (CDI-based Deployments and Tests)
* PicketLink Identity Management (CDI-based Deployments and Tests)
* PicketLink Authorization and Apache DeltaSpike (CDI-based Deployments and Tests)
* PicketLink Federation SAML
* PicketLink Identity Management Subsystem
* PicketLink Usage From JBoss Modules

The tests can be run against different JBoss EAP and WildFly versions. 

This test suite always provide tests for the latest released version of JBoss EAP 6 and WildFly and also for the current upstream 
version of those projects. In the latter case, you may need to clone and build EAP or WildFly locally in order to run the tests
for a specific version.

You also need to clone and build the [https://github.com/jboss-developer/jboss-picketlink-quickstarts](PicketLink Quickstarts). When build them
do it for both EAP and WildFly. More details in the README.

## Modules Overview

* **base**, provides tests for PicketLink Base Module
* **idm**, provides tests for PicketLink Identity Management Module
* **saml**, provides tests for PicketLink Federation SAML Support
* **subsystem**, provides tests for PicketLink Extensions and Subsystems for JBoss EAP and WildFly
 

## Running Tests in JBoss Enterprise Application Platform 6

To run the tests using JBoss EAP 6 you should execute the following command:

    mvn clean verify -Pjboss-eap -Pjboss-eap-6.3
    
The command above will execute all tests against JBoss EAP 6.3 version. The **-Pjboss-eap** profile must be always defined regardless
the EAP version you want to use. The list below summarizes all JBoss EAP supported versions:
 
* For JBoss EAP 6.2, run *mvn clean verify -Pjboss-eap **-Pjboss-eap-6.2**

* For JBoss EAP 6.3, run *mvn clean verify -Pjboss-eap **-Pjboss-eap-6.3**

**Note:** Make sure you have JBoss EAP Maven Artifacts configured in your local Maven repository. Otherwise you wont be able to 
resolve the necessary dependencies to run the tests.

## Running Tests in WildFly

To run the tests using WildFly you should execute the following command:

    mvn clean install -Pwildfly -Pwildfly-8.1.0.Final
    
The command above will execute all tests against WildFly 8.1.0.Final version. The **-Pwildfly** profile must be always defined regardless
the WildFly version you want to use. The list below summarizes all WildFly supported versions:
 
* For WildFly 8.1.0.Final, run *mvn clean install -Pwildfly **-Pwildfly-8.1.0.Final**

* For WildFly 8.2.0.CR1-SNAPSHOT, run *mvn clean install -Pwildfly **-Pwildfly-8.2.0.CR1-SNAPSHOT**

* For WildFly 9.0.0.Alpha1-SNAPSHOT, run *mvn clean install -Pwildfly **-Pwildfly-9.0.0.Alpha1-SNAPSHOT***

**Note:** Some versions may require you to clone and build WildFly locally in order to get all required dependencies. 