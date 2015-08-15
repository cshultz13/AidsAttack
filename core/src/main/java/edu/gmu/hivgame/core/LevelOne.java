package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;

import java.util.Random;
import java.util.LinkedList;
import java.util.*;
import java.math.*;
import pythagoras.f.Point;

import playn.core.Game;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.GroupLayer;
import playn.core.CanvasImage;
import playn.core.Canvas;
import playn.core.SurfaceImage;
import playn.core.Layer;
import static playn.core.PlayN.pointer;
import playn.core.Pointer;
import static playn.core.PlayN.keyboard;
import playn.core.Keyboard;
import playn.core.Key;
import playn.core.util.Callback;
import playn.core.*;

// First level of game: Cell approach
public class LevelOne extends Level{
  boolean attractingVirus;
  Virus theVirus;
  Cell theCell;
  LinkedList<Antibody> antibodiesLL;

  Vec2 virusScreenTarget = new Vec2();
  float minLength = 1f;
  float forceScale = 10f;
  Random gravity = new Random(12345);

  static LevelOne make(AidsAttack game){
    LevelOne lv = new LevelOne();
    lv.game = game;
    return lv;
  }

  void populateAntibodies(int num){
    if(this.antibodiesLL == null){
      this.antibodiesLL = new LinkedList<Antibody>();
    }
    if(this.antibodiesLL.size() == 0){
      Random r = new Random(54321);
      for(int i=0; i<num; i++){
        float x = r.nextFloat();
        System.out.println("Random x is: "+x);
        float y = r.nextFloat();
        System.out.println("Random y is: "+y);
        Antibody a = Antibody.make(this.game, this, x*50, y*50, .2f);
        antibodiesLL.addLast(a);
      }
    }
  }

  @Override
  void initLevel(Camera camera){
    super.initLevel(camera);
    gameOver = false;
    success = false;

    // create the Virus object
    this.theVirus = Virus.make(this.game, this, 5f, 0f, .2f);

    // create the Cell object
    this.theCell = Cell.make(this.game, this, 30f, 30f, .2f);

    // Random to distribute Antibodies on screen
    Random r = new Random(12345);
    populateAntibodies(5);

    // Hook up pointer listener to control Virus
    pointer().setListener(new Pointer.Adapter() {
	    @Override
      public void onPointerStart(Pointer.Event event) {
        Point p = new Point(event.x(), event.y());
        System.out.printf("Point p is at: %f, %f.\n",p.x(), p.y());
        Layer hit = game.buttonLayer.hitTest(p);
        if(hit != null){
          System.out.println("Hit a button!");
        }
        else{
          attractingVirus = true;
          virusScreenTarget.set(event.x(),
              event.y());
        }
        hit = worldLayer.hitTest(p);
        if(hit != null){
          System.out.println("WorldLayer found!");
        }
      }
      @Override
      public void onPointerEnd(Pointer.Event event) {
        attractingVirus = false;
      }
      @Override
      public void onPointerDrag(Pointer.Event event) {
        Point p = new Point(event.x(), event.y());
        System.out.printf("Point p is at: %f, %f.\n",p.x(), p.y());
        Layer hit = game.buttonLayer.hitTest(p);
        if(hit != null){
          System.out.println("Hit a button!");
        }
        else{
          attractingVirus = true;
          virusScreenTarget.set(event.x(),
            event.y());
        }
      }
    });
  }

  @Override
  void endLevel(){
    super.endLevel();
    Body bodyList = m_world.getBodyList();
    Body nextBody;
    while(bodyList != null){
      nextBody = bodyList.getNext();
      m_world.destroyBody(bodyList);
      bodyList = nextBody;
      nextBody = null;
    }
    if(antibodiesLL != null){
      antibodiesLL.clear();
    }
  }
  @Override
  void successLevel(){
    super.successLevel();
    //FIXME: This is dangerous! May cause thrown exceptions if any method attempts to remove a layer from
    //    a groupLayer after doing this.
    graphics().rootLayer().destroyAll();
  }

  String levelName() {
    return "Level One";
  }
  void updateLevel(int delta, int time){
    if(time%100 == 0){
      float r1 = (gravity.nextFloat() - 0.5f)*5f;
      float r2 = (gravity.nextFloat() - 0.5f)*5f;
      Vec2 ng = new Vec2(r1,r2);
      System.out.printf("New gravity is: %f, %f\n",r1,r2);
      m_world.setGravity(ng);
    }

    theVirus.update(delta);
    theCell.update(delta);

    if(this.attractingVirus){
      Vec2 virusPhysTarget = new Vec2();
      virusPhysTarget.set(camera.screenXToPhysX(virusScreenTarget.x),
        camera.screenYToPhysY(virusScreenTarget.y));
      theVirus.attractTowards(virusPhysTarget);
    }
    if(antibodiesLL != null){
      ListIterator<Antibody> itr = antibodiesLL.listIterator(0);
      while(itr.hasNext()){
        Antibody a = itr.next();
        if(a.isDestroyed()){
          itr.remove();
        }
        else{
          a.update(delta);
        }
      }
    }

    //Handling Contacts between fixtures. m_userData of Virus and Antibodies is themselves,
    //and they implement the interface CollisionHandler.
    Contact contact = m_world.getContactList();
    while(contact != null){
      if(contact.isTouching()){
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        if(fixtureA.m_userData instanceof CollisionHandler){
          CollisionHandler ch = (CollisionHandler) fixtureA.m_userData;
          ch.handleCollision(fixtureA, fixtureB);
        }
        if(fixtureB.m_userData instanceof CollisionHandler){
          CollisionHandler ch = (CollisionHandler) fixtureB.m_userData;
          ch.handleCollision(fixtureB, fixtureA);
        }
      }
      contact = contact.getNext();
    }
  }

  void update(int delta, int time){
    if(!gameOver && !success){
      super.update(delta, time);
      updateLevel(delta, time);
    }
    // the step delta is fixed so box2d isn't affected by framerate
    m_world.step(0.033f, 10, 10);
  }
  void paint(float alpha){
    if(!gameOver && !success){
      theVirus.paint(alpha);
      theCell.paint(alpha);
      if(antibodiesLL != null){
        ListIterator<Antibody> itr = antibodiesLL.listIterator(0);
        while(itr.hasNext()){
          itr.next().paint(alpha);
        }
      }
    }
  }
}
