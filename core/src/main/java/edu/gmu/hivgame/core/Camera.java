//Class to consolidate all zooming and translation of view during gameplay.
package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import java.math.*;

import playn.core.Game;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.GroupLayer;
import playn.core.Layer;
import static playn.core.PlayN.keyboard;
import playn.core.Keyboard;
import playn.core.Key;
import playn.core.util.Callback;

public class Camera{
  AidsAttack game;
  //a constant value for resetting the camera at the beginning of a level.
  public static final float initSUPPU = 20.0f; //init Screen Unit Per Phys Unit
  public float screenUnitPerPhysUnit = 20.0f;
  public float screenPerPhysGoal = 20.0f;
  private float zoomStep = 0.1f;
  public float translationX = 0.0f;
  public float translationY = 0.0f;
  public float tXGoal = 0.0f;
  public float tYGoal = 0.0f;
  private float tStep = 1f;
  private float tGoalStep = 10f;
  public boolean zoomingIn = false;
  public boolean zoomingOut = false;
  private Camera(){
  }
  public Camera(AidsAttack game){
    this.game = game;
  }
  public void setWorldScale(){
    this.game.currentLevel.worldLayer.setScale(screenUnitPerPhysUnit);
  }
  public void reset(){
    screenUnitPerPhysUnit = initSUPPU;
    screenPerPhysGoal = initSUPPU;
    translationX = 0.0f;
    translationY = 0.0f;
    tXGoal = 0.0f;
    tYGoal = 0.0f;
    zoomingIn = zoomingOut = false;
  }
  public float physXToScreenX(float physX){
    return (physX*screenUnitPerPhysUnit) + translationX;
  }
  public float screenXToPhysX(float screenX){
    return (screenX - translationX) / screenUnitPerPhysUnit;
  }
  public float physYToScreenY(float physY){
    return (physY*screenUnitPerPhysUnit) + translationY;
  }
  public float screenYToPhysY(float screenY){
    return (screenY - translationY) / screenUnitPerPhysUnit;
  }
  public void zoomIn(){
    if(screenPerPhysGoal < 60f){
      screenPerPhysGoal += .1f;
    }
  }
  public void zoomOut(){
    if(screenPerPhysGoal > 1f){
      screenPerPhysGoal -= .1f;
    }
  }
  public void translateLeft(){
    tXGoal -=tGoalStep;
  }
  public void translateRight(){
    tXGoal +=tGoalStep;
  }
  public void translateUp(){
    tYGoal -=tGoalStep;
  }
  public void translateDown(){
    tYGoal +=tGoalStep;
  }
  public void updateTranslation(){
    float xDiff = Math.abs(tXGoal - translationX);
    if(xDiff > tStep){
      if(translationX < tXGoal){
        translationX += tStep;
      }
      else if(tXGoal < translationX){
        translationX -= tStep;
      }
    }
    float yDiff = Math.abs(tYGoal - translationY);
    if(yDiff > tStep){
      if(translationY < tYGoal){
        translationY += tStep;
      }
      else if(tYGoal < translationY){
        translationY -= tStep;
      }
    }
  }
  public void updateZoom(){
    if(zoomingIn){
      zoomIn();
    }
    else if(zoomingOut){
      zoomOut();
    }
    float diff = Math.abs(screenPerPhysGoal - screenUnitPerPhysUnit);
    if(diff > zoomStep){
      if(screenUnitPerPhysUnit < screenPerPhysGoal){
        screenUnitPerPhysUnit += zoomStep;
      }
      else if(screenPerPhysGoal < screenUnitPerPhysUnit){
        screenUnitPerPhysUnit -= zoomStep;
      }
    }
    setWorldScale();
  }
  public void update(){
    updateZoom();
    updateTranslation();
  }
}
