# shrimpdroid

This is an example Android app, written in Clojure, that uses the
[turboshrimp library](https://github.com/wiseman/turboshrimp) to
control an AR.Drone.

![shrimpdroid screenshot](/media/screenshots/shrimpdroid-s.png?raw=true "caption")

## To install and use the app:

1. Install the Android SDK.
2. Change the `:sdk-path` key in project.clj to point to where you installed the SDK.
3. Connect your Android device to your computer.
4. Turn on your AR.Drone and connect your Androd device to the drone's wifi network.
4. Run `lein droid doall` to build, install and run the app.
5. Fly the drone with the app!
