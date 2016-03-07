# Burning Eye
Little Android game I made from scratch in 2010 - Just bought back to life.

I haven't touched it in 5 years, but it is now working on most phones.  Originally, it was built to run on Andrdoi 4-8, but I now have it running on 17-23.

The original code for this project was lost, so I had to decompile it from an APK.  Not too many issues there (decompiers are pretty fancy these days).  A lot of the constants are mislabeled, and there are a few other ugly bits that I'm still cleaning up.

# TODO List
- Tablet and super-high res support
- Better handling of orientation calibration problems (Magnemometer is crap on many phones, including mine)
- Map scaling and positioning is a little off - this was developed when there were only a couple of options for screen dimensions
- Use of fragments - entirely absent in the current version.  Should probably combine the non-game-play screens and use fragments

# Changes
- 03/07/2016
-   Tidying up ugly constants and refactoring a bit inside GameLogic.java.
-   Refactored several internal classes inside Gamelogic.java.
