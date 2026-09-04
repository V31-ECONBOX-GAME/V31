# buildSrc

The build's own code — the plugins projects apply, and the tasks they register. Ids are in `build.gradle`, one package per plugin.

`constant/` holds every name two classes have to agree on; a name only one class uses stays private to it.

```bash
./gradlew -p buildSrc test
```

`jar` depends on `check`, so a broken plugin fails before any project applies it.
