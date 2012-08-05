import processing.core.*; 
import processing.xml.*; 

import processing.opengl.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class Lines extends PApplet {



// helpful globals
int _bg = color(255, 255, 255);
int _winSizeX = 500;
int _winSizeY = 500;
ArrayList<MovingLine> _lines;

// return the sign of the number (1, -1)
public int sign(float n) { return PApplet.parseInt(n / abs(n)); }

// make a line which starts on one edge of the square, and moves inward.
// expects either from, to, or neither location
public MovingLine makeLine(PVector from, PVector to) {
  // determine the starting position
  PVector p = new PVector();
  if (from != null) {
    p = new PVector(from.x, from.y);
  } else {
    if (PApplet.parseInt(random(2)) == 0) { // on an x side
      p.x = _winSizeX * random(1);
      p.y = PApplet.parseInt(random(2)) * _winSizeY;
    } else {
      p.y = _winSizeY * random(1);
      p.x = PApplet.parseInt(random(2)) * _winSizeX;
    }
  }

  // determine the direction
  PVector d = (to != null) ? to : new PVector(_winSizeX / 2, _winSizeY / 2);
  d.sub(p);
  if (to == null && from == null) {
    // starting state: go randomly inwards
    d.x = sign(d.x) * random(.5f, 2);
    d.y = sign(d.y) * random(.5f, 2);
  } else if (to == null) {
    // going from somewhere.... so random direction (for now)
    d.x = random(2) < 1 ? random(1, 2) : random(-2, -1);
    d.y = random(2) < 1 ? random(1, 2) : random(-2, -1);
  } else {
    // going to somewhere, so maintain the direction
    d.mult(0.005f * random(1, 2));
  }

  return new MovingLine(p, d);
}

// represents a line growing at a certain speed
class MovingLine {
  PVector _s, _e;
  PVector _d;
  int _c;
  boolean _stopped = false;

  MovingLine(PVector s, PVector d) {
    _s = s;
    _e = new PVector(s.x, s.y);
    _d = d;
    _c = color(random(50, 200), random(50, 200), random(50, 200));
  }

  public PVector start() { return _s; }
  public PVector end() { return _e; }

  // true if the end directly touches l
  public boolean touches(MovingLine l) {
    float dist[] = new float[4];
    dist[0] = ptLnDist(_s, l);
    dist[1] = ptLnDist(_e, l);
    dist[2] = ptLnDist(l.start(), this);
    dist[3] = ptLnDist(l.end(), this);

    boolean intersects = sign(dist[0]) != sign(dist[1]) && sign(dist[2]) != sign(dist[3]);
    for (int i = 0; i < 4; i++) dist[i] = abs(dist[i]);
    return intersects && min(dist) == dist[1];
  }

  public void update() {
    _e.x += _d.x;
    _e.y += _d.y;
  }

  public void stop() {
    _d.x = 0;
    _d.y = 0;
    _stopped = true;
  }

  public boolean stopped() {
    return _stopped;
  }

  public float length() {
    return sqrt(pow(_s.x - _e.x, 2) + pow(_s.y - _e.y, 2));
  }

  public void display() {
    stroke(_c);
    fill(_c);
    line(_s.x, _s.y, _e.x, _e.y);
    this.update();
  }

  // returns the distance between a point and a line in 2D
  private float ptLnDist(PVector pt, MovingLine ml) {
    float xm = (ml.start().x + ml.end().x) / 2;
    float ym = (ml.start().y + ml.end().y) / 2;
    return (ml.end().y - ml.start().y) * (pt.x - xm) + (ml.start().x - ml.end().x) * (pt.y - ym);
  }
}

public void setup() {
  size(500, 500, OPENGL);
  hint(DISABLE_OPENGL_2X_SMOOTH);
  hint(ENABLE_OPENGL_4X_SMOOTH);
  smooth();

  strokeWeight(2);
  _lines = new ArrayList<MovingLine>();
}

public void mouseClicked() {
  // send a line towards the mouse location
  _lines.add(makeLine(null, new PVector(mouseX, mouseY)));
}

public void keyPressed() {
  if (key == 'c') _lines.clear();
  if (key > 49 && key < 58) {
    for (int i = 0; i < key - 48; i++) {
      _lines.add(makeLine(null, null));
    }
  }
}

public void draw() {
  background(_bg);
  ArrayList<MovingLine> startFrom = new ArrayList<MovingLine>();

  // update lines, look for collisions
  for (MovingLine l : _lines) {
    l.display();
    for (MovingLine l2 : _lines) {
      if (l != l2 && l.touches(l2) && !l.stopped() && !startFrom.contains(l)) {
        l.stop();
        startFrom.add(l);
        break;
      }
    }
  }

  // make the new lines off of stopped ones
  for (MovingLine old : startFrom) {
    if (old.length() > 40) {
      // two more lines, if the parent is small enough
      _lines.add(makeLine(old.end(), null));
      _lines.add(makeLine(old.end(), null));
    }
  }
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "Lines" });
  }
}
