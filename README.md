# AIAD 2020-21
AI course project using JADE, Repast3, SaJas and JavaFX.
**Mafia in Town** is an invented game by the group where N agents (all CPU) fight for their faction, like in `Among Us` or `Town of Salem`.

## Run the project (Release 1)
- Clone this repository for release 1
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
    
## Run the project (Release 2)
- Clone this repository for release 2
- Launch **IntelliJ IDEA** and open this folder (on this level) as a project
- Make sure you have **JADE**, **SaJas** and **Repast3** in your machine
- Go to project structure `(Ctrl + Alt + Shift + S)` 
    - Under `Project` select a language level above 8 (Lambda Expressions)
    - Then on the `Libraries` tab add **JADE**, **SaJas** and **Repast**
        - Example: `C:\Programs\JADE\lib\jade.jar`
        - Example: `C:\SAJaS/lib/SAJaS.jar`
        - Example: `C:\Repast3/RepastJ/repast.jar`
        - Example: `C:\Repast3/RepastJ/lib` (add all jars below this folder)
    - Your main class should be `launcher.GameLauncher`.
- Now you're ready to run the project. Do it by pressing the play button next to the configuration
- This project can also be ran in Batch Mode, that allows for sequential runs. Those configurations are in GameLauncher.java