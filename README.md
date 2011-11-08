DstarApp
========

*Visualizes the workings of the D\* path finding algorithm.*

Usage
-----
Select the graph center by clicking a node in the drawing canvas. Press 'Run' to start time simulation. 'Fade time' is the time (or distance) direction information flows through the graph. 'Flow time' is the time a direction is stored inside a single node. It follows that fade time >= flow time.

To simulate 'instant' pathfinding, set the flow time (and thus the fade time) to a high value, e.g. 100. To simulate scent based pathfinding (like wolves searching for a rabbit), set flow time low, e.g. 10, and fad time high, e.g. 60. Here flow time indicates how well the scent spreads by diffusion, and fade time indates how long it takes for the scent to fade away.

Build
-----
DstarApp has been developed using NetBeans IDE ([www.netbeans.org](http://www.netbeans.org/)), but source files can be compiled using other Java 6 compilers.

License
-------
DstarApp is licensed under the terms of the GNU General Public License, see the included LICENSE file.

Author
------
[Leo Vandriel](http://www.leovandriel.com)