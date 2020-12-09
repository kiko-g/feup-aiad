# AIAD 2020-21
AI course project using JADE

## Run the project
- Clone this repository
- Launch **IntelliJ IDEA** and open this folder (on this level) as a project
- Make sure you have both **JavaFX** and **JADE** in your machine
- Go to project structure `(Ctrl + Alt + Shift + S)` 
    - Under `Project` select a language level above 8 (Lambda Expressions)
    - Then on the `Libraries` tab add **JADE** and **Java FX**
        - Example: `C:\Programs\JADE\lib\jade.jar`
        - Example: `C:\Programs\JavaFX11.0.2\lib\`
    - After that, on the top right of your main window, edit your configuration
    and add `--module-path <YOUR_PATH_TO_JAVA_FX> --add-modules javafx.controls,javafx.fxml`
    to the VM Options. Your main class should be `launcher.GameLauncher`
- Now you're ready to run the project, by pressing the play button next to the configuration
    
    