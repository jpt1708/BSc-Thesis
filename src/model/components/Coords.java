package model.components;

public class Coords {

        private double x;
        private double y;

        public boolean equals(Object o) {
            Coords c = (Coords) o;
            return c.x == x && c.y == y;
        }

        public Coords(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Coords getCoords(){
        	return this;
        }
        
        public double getX(){
        	return this.x;
        }
        public double getY(){
        	return this.y;
        }
    }
