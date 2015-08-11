package edu.gmu.hivgame.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.Joint;
import static playn.core.PlayN.*;
import java.util.*;
import java.util.Random;

//A single strand of DNA or RNA.
//Double-stranded DNA is implemented with two linked DNAStrands.
//contains a linked list of nucleotides, and distance joints between two adjacent nucleotides.
public class DNAStrand{
  private AidsAttack game;
  private LevelTwo level;
  private LinkedList<Nucleotide> strand;

  private DNAStrand(AidsAttack game, Level level){
    this.game = game;
    this.level = (LevelTwo) level;
    strand = new LinkedList<Nucleotide>();
  }
  public static DNAStrand make(AidsAttack game, Level level, float x, float y, int length){
    DNAStrand s = new DNAStrand(game, level);
    BodyDef bodyDef = new BodyDef();
    //Nucleotide n = Nucleotide.make(game,level,Nucleobase.valueOf("C"),x, y, 0f);
    //Nucleotide n2 = Nucleotide.make(game,level,Nucleobase.valueOf("T"),x+1.1f, y, 0f);
    //s.addLast(n);
    //s.addLast(n2);
    s.populateStrand(length, x, y);
    return s;
  }

  private void populateStrand(int length, float x, float y){
    Random r = new Random();
    Nucleobase [] bases = Nucleobase.values();
    int i;
    float separationDist = 1.5f;
    int baseChoice;
    for(i=0; i<length; i++){
      baseChoice = Math.abs(r.nextInt()) % bases.length;
      Nucleotide n = Nucleotide.make(this.game, this.level, bases[baseChoice], x+separationDist*i, y, 0f);
      this.addLast(n);
    }
  }

  private void addLast(Nucleotide n){
    if(strand.size() > 0){
      Nucleotide prev = strand.getLast();
      prev.strandLink(n);
    }
    strand.addLast(n);
  }

  public void addNucleotide(Nucleotide n){
    this.addLast(n);
  }
  public ListIterator<Nucleotide> getIterator(){
    ListIterator<Nucleotide> itr = strand.listIterator(0);
    return itr;
  }

  // no physical body itself, so need to update each individual Nucleotide
  public void update(int delta){
    if(strand.size() == 0){
      return;
    }
    ListIterator<Nucleotide> iterator = strand.listIterator(0);
    while(iterator.hasNext()){
      iterator.next().update(delta);
    }
  }
  public void paint(float alpha){
    if(strand.size() == 0){
      return;
    }
    ListIterator<Nucleotide> iterator = strand.listIterator(0);
    while(iterator.hasNext()){
      iterator.next().paint(alpha);
    }
  }
}
