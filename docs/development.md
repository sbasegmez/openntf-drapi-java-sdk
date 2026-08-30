# Development

## Testing

This project uses Mockito for mock testing. Maven is configured to run tests automatically during the build process.

If you are going to run tests manually (or on your IDE), do not forget to add Mockito agent to your VM options. You can do this by adding the following line to your VM options:

```
-javaagent:/path/to/mockito-core-5.23.0.jar
```

You can directly use Maven repository for this option:

```
-javaagent:/Users/<your-username>/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar
```

Note that the version of Mockito may change over time, so make sure to use the correct version that matches your project's dependencies.


