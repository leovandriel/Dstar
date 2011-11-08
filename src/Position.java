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

/** Basic 2D integer vector. */
public class Position
{

   public int x;
   public int y;

   public Position(int x, int y)
   {
      this.x = x;
      this.y = y;
   }

   @Override
   public String toString()
   {
      return x + ", " + y;
   }
}
