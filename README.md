# CSE535Group7Project1
A Health monitoring App


# Project Notes

Used the [Simple Graph Library](http://www.android-graphview.org/simple-graph/). Used the dependency method to add as dependency. Here is an [example for real-time integration](http://www.android-graphview.org/realtime-chart/).

Note: The Graph uses a special structure defined in Graph.xml file.

In order to run the file, first do a gradle plugin synch and then build and run the app.

# @Status

1. DB file and table creation ->working.
2. accelerometer integration -> working.
3. accelerometer values storage in DB -> working
4. accelerometer values retreival from DB -> working
5. fields validation -> working.
6. table creation on filling fields(name, sex, age, id) in any order.

# @Kinks

1. sampling frequency -> delay implemented for 1 second. still not perfectly fine.
2. DB file and folder get created on the emulator, but not on device. 
 

# @ToDo

1. Graph plotting from sensor values retrieval.
2. Upload
3. Download
4. Testing
