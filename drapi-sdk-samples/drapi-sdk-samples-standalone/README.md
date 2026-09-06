# Drapi SDK Samples - Standalone Java Application


This directory contains sample code demonstrating how to use the Drapi SDK in a standalone Java application to interact with the HCL Domino REST API. The samples cover common use cases and provide practical examples for developers to get started quickly.

## Setup and Configuration

Before running the samples, you need to set up the Drapi SDK and configure it to connect to your HCL Domino server. Follow these steps:

Before running `StandaloneExample`, copy `src/main/resources/config/drapi-sdk-template.properties` to `src/main/resources/config/drapi-sdk-sample.properties` in this module, then edit the copy with your server details: set `BASEURL` to your DRAPI server's base URL, and provide either `UserName`/`Password` for basic authentication or a `Token` if you already have one. 

The `drapi-sdk-sample.properties` file is excluded from version control (see `.gitignore`), so your credentials stay local. At startup, `DrapiConfig.builder().applyResourceFile("config/drapi-sdk-sample.properties")` loads this file from the classpath, so it must be under `src/main/resources` before you build or run the sample. 

For a ready-made database to test against, you can use the [OpenNTF Projects Dataset](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF+Projects+Dataset) — it already matches the `name`, `overview`, and `chefs` fields the example prints, so set it up as a DRAPI scope named `projects` to match the sample's `client.dataSource("projects")` call.
