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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/** Canvas to draw the square grid on. The programs main drawing canvas. */
public class MapCanvas extends Canvas implements Runnable
{

   // coords of the big arrow
   private final static float[] arrowFillX = new float[]
   {
      0, .5f, .1f, .1f, -.1f, -.1f, -.5f
   };
   private final static float[] arrowFillY = new float[]
   {
      1f, .4f, .4f, -1f, -1f, .4f, .4f
   };
   // coords of the small arrow
   private final static float[] arrowLineX = new float[]
   {
      0, 0, 0, -.5f, 0f, .5f
   };
   private final static float[] arrowLineY = new float[]
   {
      -1f, 1f, 1f, .4f, 1f, .4f
   };
   private final static float arrowLength = .3f;
   private final static float targetRadius = .4f;
   // the D* model
   private DstarGrid grid = null;
   // flag to indicate running = active paint thread
   private boolean running = false;
   // double buffered graphics
   private BufferedImage imageBuffer = null;
   private Graphics graphicsBuffer = null;
   // the canvas size
   private int width = 0;
   private int height = 0;
   private Thread currentThread = null;
   private int threadPriority = Thread.MIN_PRIORITY;

   public MapCanvas(DstarGrid grid)
   {
      this.grid = grid;
   }

   // paint the grid
   @Override
   public void paint(Graphics g)
   {
      // create buffer if needed
      if (width != this.getWidth() || height != this.getHeight())
      {
         width = this.getWidth();
         height = this.getHeight();
         imageBuffer = (BufferedImage) this.createImage(width, height);
         graphicsBuffer = imageBuffer.createGraphics();
      }

      // read colors, directions, targets
      Color[][] colorMap = grid.getColorMap();
      float[][] directionMap = grid.getDirectionMap();
      Position[] targets = grid.getTargets();

      // draw the grid

      int mapWidth = grid.getWidth();
      int mapHeight = grid.getHeight();

      float scaleWidth = width / (float) mapWidth;
      float scaleHeight = height / (float) mapHeight;

      // init transformation buffer
      int[] transformedX = new int[arrowFillX.length];
      int[] transformedY = new int[arrowFillX.length];

      // for all tiles
      for (int y = 0; y < mapHeight; y++)
      {
         for (int x = 0; x < mapWidth; x++)
         {

            // draw tile colors
            graphicsBuffer.setColor(colorMap[x][y]);

            graphicsBuffer.fillRect((int) (x * scaleWidth), (int) (y * scaleHeight), (int) (scaleWidth + 1), (int) (scaleHeight + 1));

            // draw arrow
            if (directionMap[x][y] >= 0)
            {
               // calc transformation
               float cos = arrowLength * (float) Math.cos(directionMap[x][y]);
               float sin = arrowLength * (float) Math.sin(directionMap[x][y]);

               graphicsBuffer.setColor(Color.black);
               // tile contains enough pixels
               if (scaleWidth * scaleHeight > 1000)
               {
                  // draw big arrow
                  for (int i = 0; i < arrowFillX.length; i++)
                  {
                     transformedX[i] = (int) (scaleWidth * (x + .5f + cos * arrowFillX[i] - sin * arrowFillY[i]) + .5f);
                     transformedY[i] = (int) (scaleHeight * (y + .5f + sin * arrowFillX[i] + cos * arrowFillY[i]) + .5f);
                  }
                  graphicsBuffer.fillPolygon(transformedX, transformedY, arrowFillX.length);
               }
               else
               {
                  // draw small arrow
                  for (int i = 0; i < arrowLineY.length - 1; i += 2)
                  {
                     int beginx = (int) (scaleWidth * (x + .5f + cos * arrowLineX[i] - sin * arrowLineY[i]) + .5f);
                     int beginy = (int) (scaleHeight * (y + .5f + sin * arrowLineX[i] + cos * arrowLineY[i]) + .5f);
                     int endx = (int) (scaleWidth * (x + .5f + cos * arrowLineX[i + 1] - sin * arrowLineY[i + 1]) + .5f);
                     int endy = (int) (scaleHeight * (y + .5f + sin * arrowLineX[i + 1] + cos * arrowLineY[i + 1]) + .5f);
                     graphicsBuffer.drawLine(beginx, beginy, endx, endy);
                  }
               }
            }

            // draw targets
            for (int i = 0; i < targets.length; i++)
            {
               Color c = colorMap[targets[i].x][targets[i].y];
               int max = c.getRed();
               if (c.getGreen() < max)
               {
                  max = c.getGreen();
               }
               if (c.getBlue() < max)
               {
                  max = c.getBlue();
               }
               graphicsBuffer.setColor(new Color(255 - max, 255 - max, 255 - max));
               graphicsBuffer.fillOval((int) (scaleWidth * (.5f + targets[i].x - targetRadius) + .5f), (int) (scaleHeight * (.5f + targets[i].y - targetRadius) + .5f), (int) (scaleWidth * 2 * targetRadius + .5f), (int) (scaleHeight * 2 * targetRadius + .5f));
            }

         }
      }

      // draw the buffer
      g.drawImage(imageBuffer, 0, 0, this);

   }

   // return colliding tile position
   public Position parseMouse(int x, int y)
   {
      Position p = new Position(x * grid.getWidth() / this.getWidth(), y * grid.getHeight() / this.getHeight());
      if (p.x < 0 || p.x >= grid.getWidth() || p.y < 0 || p.y >= grid.getHeight())
      {
         return null;
      }
      return p;
   }

   // java internal
   @Override
   public void update(Graphics g)
   {
      paint(g);
   }

   public float[] getArrowFillX()
   {
      return arrowFillX;
   }

   public void setThreadPriority(int threadPriority)
   {
      this.threadPriority = threadPriority;
      if (currentThread != null)
      {
         currentThread.setPriority(threadPriority);
      }
   }

   // start the grid and repaint
   public void start()
   {
      currentThread = new Thread(this);
      currentThread.setPriority(threadPriority);
      running = true;
      currentThread.start();
   }

   // start the grid and repaint
   public void step()
   {
      grid.step();
      repaint();
   }

   // start the stop and repaint
   public void stop()
   {
      running = false;
   }

   // indicates process active
   public boolean running()
   {
      return running;
   }

   public void run()
   {
      grid.start();

      // update the grid
      // if running, repeat drawing
      while (running)
      {
         grid.move(10);
         repaint();
         try
         {
            Thread.sleep((long) (grid.period * 1000));
         }
         catch (Exception e)
         {
            System.out.println(e);
         }
      }

      currentThread = null;
      grid.stop();
   }
}
