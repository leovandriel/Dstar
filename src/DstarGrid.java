//==================================================================================================
//
// DstarApp v.1 - To visualize the workings of the D* path search algorithm.
//
// Copyright (C) 2008  Leo Vandriel  (mail@leovandriel.com)
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//==================================================================================================
package dstarapp;

import java.awt.Color;
import java.util.*;

/** The search graph. Core D* algorithm in here. */
public class DstarGrid
{

   public final static Position[] dir4 = new Position[]
   {
      new Position(0, 1),
      new Position(1, 0),
      new Position(0, -1),
      new Position(-1, 0),
   };
   public final static Position[] dir4diag = new Position[]
   {
      new Position(1, 1),
      new Position(1, -1),
      new Position(-1, 1),
      new Position(-1, -1)
   };
   public final static Position[] dir8 = new Position[]
   {
      new Position(0, 1),
      new Position(1, 0),
      new Position(0, -1),
      new Position(-1, 0),
      new Position(1, 1),
      new Position(1, -1),
      new Position(-1, 1),
      new Position(-1, -1)
   };
   // all possible directions
   private Position[] dir = null;
   private float[] dirlength = null;
   // States: 0 = empty, 12345 = direction
   private int[][] tileState = null;
   // time is used to get the most recent information
   private float[][] tileTime = null;
   // the speed on can make on a tile
   private float[][] tileSpeed = null;
   // grid size
   private int width = 0;
   private int height = 0;
   // keep up the remaining time
   private float waitTime = 0;
   // the last time move was called
   private long lastTime = -1;
   // list of target and its attractor
   private Vector<Position> targetList = null;
   private Vector<Position> attractorList = null;
   // all instances that want to be updated during successive moves
   private Vector<Updateable> updateableList = null;

   // The fadeTime (in seconds) is the time it takes for the targets scent to fade away.
   public float fadeTime = 10;
   // The flowTime (in seconds) is the time that the scents flows around after the target passed.
   public float flowTime = 10;
   // The time (in seconds) between two updates.
   public float period = 1f;

   // Constructs a DstarGrid, tracing objects by propagating directions over time
   public DstarGrid()
   {
      targetList = new Vector<Position>(2);
      attractorList = new Vector<Position>(2);

      updateableList = new Vector<Updateable>();

   }

   public void addUpdateable(Updateable updateable)
   {
      updateableList.add(updateable);
   }

   public void addTarget(Position target, Position attractor)
   {
      if (target != null && attractor != null)
      {
         targetList.add(target);
         attractorList.add(attractor);
      }
   }

   public Position[] getTargets()
   {
      Position[] result = new Position[targetList.size()];
      targetList.toArray(result);
      return result;
   }

   public float getSpeed(int x, int y)
   {
      if (x < 0 || x >= width || y < 0 || y >= height)
      {
         return -1;
      }
      return tileSpeed[x][y];
   }

   public float getTime(int x, int y)
   {
      if (x < 0 || x >= width || y < 0 || y >= height)
      {
         return -1;
      }
      return tileTime[x][y];
   }

   public String getDirection(int x, int y)
   {
      if (x < 0 || x >= width || y < 0 || y >= height)
      {
         return "none";
      }
      if (tileState[x][y] == 0 || tileState[x][y] > dir.length)
      {
         return "none";
      }
      Position direction = dir[tileState[x][y] - 1];
      return "[" + direction.x + "," + (direction.y) + "]";
   }

   public Color[][] getColorMap()
   {
      Color[][] result = new Color[width][height];

      for (int y = 0; y < height; y++)
      {
         for (int x = 0; x < width; x++)
         {
            float time = tileTime[x][y] / fadeTime;
            if (tileState[x][y] == 0)
            {
               time = 1;
            }
            if (time > 1)
            {
               time = 1;
            }
            if (time < 0)
            {
               time = 0;
            }
            float speed = (float) Math.sqrt(tileSpeed[x][y]);
            if (speed > 1)
            {
               speed = 1;
            }
            if (speed < 0)
            {
               speed = 0;
            }
            result[x][y] = Color.getHSBColor(0, 1 - time, speed);
         }
      }


      return result;
   }

   public float[][] getDirectionMap()
   {
      float[][] result = new float[width][height];

      for (int y = 0; y < height; y++)
      {
         for (int x = 0; x < width; x++)
         {

            if (tileState[x][y] == 0 || tileState[x][y] > dir.length)
            {
               result[x][y] = -1;
            }
            else
            {
               Position direction = dir[tileState[x][y] - 1];
               if (direction.y > 0)
               {
                  result[x][y] = (float) -Math.atan(direction.x / direction.y) + 6.283185f;
               }
               else if (direction.y < 0)
               {
                  result[x][y] = (float) -Math.atan(direction.x / direction.y) + 3.141593f;
               }
               else
               {
                  result[x][y] = direction.x > 0 ? 4.712389f : 1.570796f;
               }
            }
         }
      }

      return result;
   }

   public int getWidth()
   {
      return width;
   }

   public int getHeight()
   {
      return height;
   }

   public void setSpeed(int x, int y, float speed)
   {
      if (x < 0 || x >= width || y < 0 || y >= height)
      {
         return;
      }
      tileSpeed[x][y] = speed;
   }

   public void setSpeed(float speed)
   {
      for (int y = 0; y < height; y++)
      {
         for (int x = 0; x < width; x++)
         {
            setSpeed(x, y, speed);
         }
      }
   }

   // (re)set the size of the grid
   public void setSize(int width, int height)
   {
      this.width = width;
      this.height = height;

      tileState = new int[width][height];
      tileTime = new float[width][height];
      tileSpeed = new float[width][height];

      for (int y = 0; y < height; y++)
      {
         for (int x = 0; x < width; x++)
         {
            tileState[x][y] = 0;
            tileTime[x][y] = 0;
            tileSpeed[x][y] = 0;
         }
      }

      targetList = new Vector<Position>(2);
      attractorList = new Vector<Position>(2);
   }

   public void setConnection(Position[] dir)
   {
      this.dir = dir;
      dirlength = new float[dir.length];
      for (int i = 0; i < dir.length; i++)
      {
         dirlength[i] = (float) Math.sqrt(dir[i].x * dir[i].x + dir[i].y * dir[i].y);
      }
      for (int y = 0; y < height; y++)
      {
         for (int x = 0; x < width; x++)
         {
            if (tileTime[x][y] > 0)
            {
               tileState[x][y] = (int) (Math.random() * dir.length + 1);
            }
         }
      }
   }

   public void randomize(float p)
   {

      for (int y = 0; y < height; y++)
      {
         for (int x = 0; x < width; x++)
         {
            if (x == 0 || y == 0 || x == width - 1 || y == height - 1)
            {
               tileSpeed[x][y] = 0;
            }
            else
            {
               tileSpeed[x][y] = Math.random() < p ? 0 : 1;
            }
         }
      }

      for (int i = 0; i < targetList.size(); i++)
      {
         Position position = targetList.get(i);
         tileSpeed[position.x][position.y] = 1;
         tileSpeed[position.x + 1][position.y] = 1;
         tileSpeed[position.x][position.y + 1] = 1;
         tileSpeed[position.x - 1][position.y] = 1;
         tileSpeed[position.x][position.y - 1] = 1;
      }
   }

   //
   // Simulation ticking
   //

   public void start()
   {
      lastTime = System.nanoTime();
   }

   public void stop()
   {
      lastTime = -1;
   }

   public void step()
   {
      moveTime(period);
   }

   // success simulation time according to clock time
   public void move(int maxFrames)
   {
      if (lastTime == -1)
      {
         lastTime = System.nanoTime();
         return;
      }

      long currentTime = System.nanoTime();
      float timeStep = 1e-9f * (currentTime - lastTime);
      lastTime = currentTime;

      if (maxFrames > 0 && timeStep > period * maxFrames)
      {
         timeStep = period * maxFrames;
      }
      moveTime(timeStep);
   }

   // make as much D* steps as time has passed, and update GUI
   private void moveTime(float timeStep)
   {

      waitTime += timeStep;

      // for remaining periods
      do
      {
         move();
         waitTime -= period;
      } while (waitTime > period + 1e-9);

      for (int i = 0; i < updateableList.size(); i++)
      {
         updateableList.get(i).update();
      }
   }

   // one step in the D* algorithm
   private void move()
   {

      float[][] tempTime = null;
      int[][] tempState = null;

      // update the target position
      for (int i = 0; i < targetList.size(); i++)
      {
         Position position = targetList.get(i);
         Position attractor = attractorList.get(i);

         // move target
         int dx = attractor.x - position.x;
         int dy = attractor.y - position.y;

         if (dx > 0 && position.x < width - 1)
         {
            position.x++;
         }
         if (dx < 0 && position.x > 0)
         {
            position.x--;
         }
         if (dy > 0 && position.y < height - 1)
         {
            position.y++;
         }
         if (dy < 0 && position.y > 0)
         {
            position.y--;
         // put target
         }
         tileState[position.x][position.y] = dir.length + 1;

         // reset time
         tileTime[position.x][position.y] = 0f;
      }


      // init spatial buffer
      tempTime = new float[width][height];
      tempState = new int[width][height];

      int offset, otherX, otherY;
      float newtime;
      // update each tile
      for (int y = 0; y < height; y++)
      {
         for (int x = 0; x < width; x++)
         {

            // only update cells with speed>0
            if (tileSpeed[x][y] > 0)
            {
               // pick offset
               if (tileState[x][y] == 0)
               {

                  tempTime[x][y] = 0;
                  tempState[x][y] = 0;

                  offset = (int) (Math.random() * dir.length);

                  // update temp element
                  for (int j = 0; j < dir.length; j++)
                  {
                     int i = (j + offset) % dir.length;
                     otherX = x + dir[i].x;
                     otherY = y + dir[i].y;
                     // if other exists
                     if (otherX >= 0 && otherX < width && otherY >= 0 && otherY < height)
                     {
                        // if other is candidate
                        if (tileState[otherX][otherY] != 0 && tileTime[otherX][otherY] <= flowTime)
                        {
                           newtime = tileTime[otherX][otherY] + .5f * dirlength[i] * (1 / tileSpeed[otherX][otherY] + 1 / tileSpeed[x][y]);
                           // if other is best candidate
                           if (tempState[x][y] == 0 || newtime < tempTime[x][y])
                           {
                              tempTime[x][y] = newtime;
                              tempState[x][y] = i + 1;
                           }
                        }
                     }
                  }

               }
               else if (tileState[x][y] <= dir.length)
               {

                  // calc new time in case nothing changes

                  tempState[x][y] = tileState[x][y];
                  tempTime[x][y] = tileTime[x][y] + 1 / tileSpeed[x][y];

                  offset = tileState[x][y] - 1;

                  // update temp element
                  for (int j = 0; j < dir.length; j++)
                  {
                     int i = (j + offset) % dir.length;
                     otherX = x + dir[i].x;
                     otherY = y + dir[i].y;
                     // if other exists
                     if (otherX >= 0 && otherX < width && otherY >= 0 && otherY < height)
                     {
                        // if other is candidate
                        if (tileState[otherX][otherY] != 0 && tileTime[otherX][otherY] <= flowTime)
                        {
                           //if(tileState[otherX][otherY] == 5 || dir[tileState[otherX][otherY]-1].x != -dir[i].x || dir[tileState[otherX][otherY]-1].y != -dir[i].y)
                           newtime = tileTime[otherX][otherY] + .5f * dirlength[i] * (1 / tileSpeed[otherX][otherY] + 1 / tileSpeed[x][y]);
                           // if other is best candidate
                           if (newtime < tempTime[x][y])
                           {
                              tempTime[x][y] = newtime;
                              tempState[x][y] = i + 1;
                           }
                        }
                     }
                  }

               }

               // if out of time range..
               if (tempTime[x][y] > fadeTime)
               {
                  tempState[x][y] = 0;
               }
            }
         }
      }

      // swap buffers.
      tileTime = tempTime;
      tileState = tempState;
   }
}


