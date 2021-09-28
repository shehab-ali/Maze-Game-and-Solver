import java.util.ArrayDeque;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.HashMap;
import java.util.Random;

/* INSTRUCTIONS ON HOW TO PLAY:
 * 
 * Using the arrow keys will move the player node either up, down, left, or right
 * 
 * Pressing "r" will reset the maze but retain the same dimensions
 * 
 * Pressing "d" will initiate a depth-first search that can be paused at any time
 *  by pressing the same button
 *  
 * Pressing "b" will initiate a breadth-first search that can be paused at any time
 *  by pressing the same button
 *  
 *  Refer to the end of the program at line 1506 for further instructions on
 *  how to adjust the size of the maze
 * 
 */

//represents a Node(cell) in a maze
class Node {
  int x;
  int y;
  Node top;
  Node bottom;
  Node left;
  Node right;
  Posn pos;

  // constructor for Node that sets its right, left, top, and bottom to null
  Node(int x, int y) {
    this.x = x;
    this.y = y;
    pos = new Posn(x, y);
  }

  /**
   * sets the top, right, left, and bottom of the given Node
   * 
   * @param board  is the board that contains the node
   * @param i      the position of the board in the arraylist
   * @param height the height position of the node on the board
   * @param width  the width of the position of the node on the board
   */
  void setNode(ArrayList<Node> board, int i, int height, int width) {
    // setting left cells
    if (this.x == 0) {
      this.left = this;
    }
    else {
      this.left = board.get(i - height);
    }

    // setting right cells
    if (this.x == width - 1) {
      this.right = this;
    }
    else {
      this.right = board.get(i + height);
    }

    // setting top cells
    if (this.y == 0) {
      this.top = this;
    }
    else {
      this.top = board.get(i - 1);
    }

    // setting bottom cells
    if (this.y == height - 1) {
      this.bottom = this;
    }
    else {
      this.bottom = board.get(i + 1);
    }
  }

}

/**
 * represents the Edges(walls) of the maze.
 *
 */
class Edge {
  /**
   * An edge contains 2 nodes that are connected horizontallyor vertically.
   */
  Node first;
  Node second;

  /**
   * Weight of the edge that will be randomized later and use kruskal's algorithm
   * to make a maze.
   */
  int weight;

  /**
   * constructor that makes an Edge without a weight.
   * 
   * @param first  the first node that connects to the second node.
   * @param second the second node that connects to the first node to make an
   *               edge.
   */
  Edge(Node first, Node second) {
    this.first = first;
    this.second = second;
  }

  /**
   * constructor that makes an Edge without a weight.
   * 
   * @param first  the first node that connects to the second node.
   * @param second the second node that connects to the first node to make an
   *               edge.
   * @param weight the weight of the edge
   */
  Edge(Node first, Node second, int weight) {
    this.first = first;
    this.second = second;
    this.weight = weight;
  }

}

/**
 * comparator class that compares edges by their weights.
 * 
 * @author sheha
 *
 */
class EdgeComparator implements Comparator<Edge> {

  /**
   * returns an int that represents the different between two edges' weights.
   * 
   * @param e1 the edge to be compared
   * @param e2 the edge to be compared
   */
  public int compare(Edge e1, Edge e2) {
    return e1.weight - e2.weight;
  }

}

/**
 * represents the whole maze that is being built Maze extends the World class
 * that makes the visual representation of the game.
 */
class Maze extends World {
  // Values can not be privatized on finalized for testing purposes
  int height;
  int width;
  ArrayList<Node> nodes = new ArrayList<Node>();
  ArrayList<Node> solution = new ArrayList<Node>();
  ArrayList<Node> movements = new ArrayList<Node>();
  Deque<Node> workList = new ArrayDeque<Node>();
  HashMap<Node, Edge> cameFromEdge = new HashMap<Node, Edge>();

  boolean depthInit = false;
  boolean breadthInit = false;

  ArrayList<Edge> edges = new ArrayList<Edge>();

  ArrayList<Edge> mst;

  HashMap<Posn, Posn> map;
  Random rand = new Random();
  Node player;

  /**
   * constructor to test setMaze.
   * 
   * @param height of the maze.
   * @param width  of the maze.
   * @param rand   number that is used to mock Math.random().
   */
  Maze(int height, int width, Random rand) {
    this.rand = rand;
    this.height = height;
    this.width = width;

    // initializes nodes and edges
    this.setMaze();

    // the node the user uses located at the beginning of the maze
    this.player = this.nodes.get(0);
  }

  /**
   * random maze constructor with a given height and width.
   * 
   * @param height of the maze.
   * @param width  of the maze.
   * @param rand   number that is used to mock Math.random().
   */
  Maze(int height, int width) {
    this.height = height;
    this.width = width;
    // initializes nodes and edges
    this.setMaze();
    // sorts edges by their random weights
    this.sortEdges();
    // creates a minimal spanning tree of the edges
    this.createMST();
    // creates the final edges by removing the minimal spanning tree's edges
    this.edges.removeAll(this.mst);

    // the node the user uses located at the beginning of the maze
    this.player = this.nodes.get(0);

    // adds the first node to the worklist
    this.workList.add(this.nodes.get(0));

  }

  /**
   * initializes nodes and edges.
   */
  public void setMaze() {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        this.nodes.add(new Node(i, j));
      }
    }

    for (int i = 0; i < this.nodes.size(); i++) {
      if (i < (this.height * (this.width - 1))) {
        this.edges.add(
            new Edge(this.nodes.get(i), this.nodes.get(i + this.height), rand.nextInt(1000) + 1));
      }
      if ((i + 1) % this.height != 0) {
        this.edges.add(new Edge(this.nodes.get(i), this.nodes.get(i + 1), rand.nextInt(1000) + 1));
      }
    }

    for (int i = 0; i < this.nodes.size(); i++) {
      this.nodes.get(i).setNode(this.nodes, i, this.height, this.width);
    }

  }

  /**
   * makes initial representations of the posn's of the edges and then performs
   * kruskal's algorithm
   */
  public void createMST() {
    mst = new ArrayList<Edge>();
    map = new HashMap<Posn, Posn>();

    // makes initial representations of the posn's of the edges
    for (int i = 0; i < this.nodes.size(); i++) {
      map.put(this.nodes.get(i).pos, this.nodes.get(i).pos);
    }

    int numEdges = 0;
    int numVertices = 0;

    // ALTERNATIVE SOLUTION
    // prim's algorithm
    /*
     * // continues to adjust the MST for (int i = 0; i < edges.size(); i++) { Edge
     * ed = this.edges.get(i); Posn p1 = ed.first.pos; Posn p2 = ed.second.pos;
     *
     * if (!this.find(p1).equals(this.find(p2))) { mst.add(ed);
     * this.map.put(this.find(p1), this.find(p2)); } }
     */

    // kruskal's algorithm
    while (numEdges < edges.size() - 1 && numVertices < (height * width)) {
      Edge ed = this.edges.get(numEdges);
      Posn p1 = ed.first.pos;
      Posn p2 = ed.second.pos;

      if (!this.find(p1).equals(this.find(p2))) {
        mst.add(ed);
        this.map.put(this.find(p1), this.find(p2));
        numVertices++;
      }
      numEdges++;
    }

  }

  /**
   * finds if the given Posn is in the map.
   * 
   * @param p the posn to be found
   * @return the posn within the map.
   */
  public Posn find(Posn p) {
    if (map.get(p).equals(p)) {
      return map.get(p);
    }
    else {
      return find(map.get(p));
    }
  }

  /**
   * sorts edges by their random weights
   */
  public void sortEdges() {
    this.edges.sort(new EdgeComparator());
  }

  /**
   * creates the world scene for the maze
   */
  public WorldScene makeScene() {
    int nodeS = 1275 / (this.width + this.height);

    WorldScene sc = new WorldScene(this.width * nodeS * 2, this.height * nodeS * 2);

    WorldImage background = // new RectangleImage(1280, 1280, "solid", Color.LIGHT_GRAY);
        new RectangleImage(this.width * nodeS * 2, this.height * nodeS * 2, "solid",
            Color.LIGHT_GRAY);

    WorldImage tl = new RectangleImage(nodeS, nodeS, "solid", Color.green);
    WorldImage br = new RectangleImage(nodeS, nodeS, "solid", Color.magenta);
    sc.placeImageXY(background, 0, 0);

    sc.placeImageXY(tl, nodeS / 2, nodeS / 2);
    sc.placeImageXY(br, nodeS * this.width - nodeS / 2, nodeS * this.height - nodeS / 2);

    for (int i = 0; i < this.edges.size(); i++) {
      Edge cur = this.edges.get(i);

      if (new Posn(0, 1).equals(distanceFromPosn(cur.first, cur.second))) {
        WorldImage line = new RectangleImage(nodeS, 1, "solid", Color.black);
        sc.placeImageXY(line, nodeS * cur.second.x + nodeS / 2, nodeS * cur.second.y);
      }
      else if (new Posn(1, 0).equals(distanceFromPosn(cur.first, cur.second))) {
        WorldImage line = new RectangleImage(1, nodeS, "solid", Color.black);
        sc.placeImageXY(line, nodeS * cur.second.x, nodeS * cur.second.y + nodeS / 2);
      }
    }

    for (int i = 0; i < this.movements.size(); i++) {
      WorldImage node = new RectangleImage(nodeS - 1, nodeS - 1, "solid", Color.cyan);
      sc.placeImageXY(node, nodeS * this.movements.get(i).x + nodeS / 2,
          nodeS * this.movements.get(i).y + nodeS / 2);

    }

    for (int i = 0; i < this.solution.size(); i++) {
      WorldImage node = new RectangleImage(nodeS - 1, nodeS - 1, "solid", Color.blue);
      sc.placeImageXY(node, nodeS * this.solution.get(i).x + nodeS / 2,
          nodeS * this.solution.get(i).y + nodeS / 2);

    }
    WorldImage playerN = new RectangleImage(nodeS - 1, nodeS - 1, "solid", Color.black);
    sc.placeImageXY(playerN, nodeS * this.player.x + nodeS / 2, nodeS * this.player.y + nodeS / 2);

    return sc;
  }

  /**
   * onKeyEvent function for bigbang.
   */
  public void onKeyEvent(String k) {
    if (k.equals("left")) {
      for (Edge cur : this.mst) {
        if ((this.player.left.equals(cur.first) && this.player.equals(cur.second))
            || (player.left.equals(cur.second) && player.equals(cur.first))) {
          this.movements.add(this.player);
          this.player = this.player.left;
        }
      }

    }

    if (k.equals("right")) {
      for (Edge cur : this.mst) {
        if ((this.player.right.equals(cur.first) && this.player.equals(cur.second))
            || (player.right.equals(cur.second) && player.equals(cur.first))) {
          this.movements.add(this.player);
          this.player = this.player.right;

        }
      }

    }

    if (k.equals("up")) {
      for (Edge cur : this.mst) {
        if ((this.player.top.equals(cur.first) && this.player.equals(cur.second))
            || (player.top.equals(cur.second) && player.equals(cur.first))) {
          this.movements.add(this.player);
          this.player = this.player.top;

        }
      }

    }

    if (k.equals("down")) {
      for (Edge cur : this.mst) {
        if ((this.player.bottom.equals(cur.first) && this.player.equals(cur.second))
            || (player.bottom.equals(cur.second) && player.equals(cur.first))) {
          this.movements.add(this.player);
          this.player = this.player.bottom;

        }
      }

    }

    if (k.equals("d")) {
      this.depthInit = !this.depthInit;
    }

    if (k.equals("b")) {
      this.breadthInit = !this.breadthInit;
    }

    if (k.equals("r")) {
      this.nodes = new ArrayList<Node>();
      this.solution = new ArrayList<Node>();
      this.movements = new ArrayList<Node>();
      this.workList = new ArrayDeque<Node>();
      this.cameFromEdge = new HashMap<Node, Edge>();

      this.depthInit = false;
      this.breadthInit = false;

      this.edges = new ArrayList<Edge>();

      // initializes nodes and edges
      this.setMaze();
      // sorts edges by their random weights
      this.sortEdges();
      // creates a minimal spanning tree of the edges
      this.createMST();
      // creates the final edges by removing the minimal spanning tree's edges
      this.edges.removeAll(this.mst);

      // the node the user uses located at the beginning of the maze
      this.player = this.nodes.get(0);

      // adds the first node to the worklist
      this.workList.add(this.nodes.get(0));
    }

  }

  /**
   * searches for a path using breadth or depth algorithm based on whether the
   * depthInit boolean or breadthInit boolean has been made to true
   */
  public ArrayList<Node> getSolution() {
    ArrayList<Node> tempMovements = this.movements;

    this.solution = new ArrayList<Node>();
    this.movements = new ArrayList<Node>();
    this.cameFromEdge = new HashMap<Node, Edge>();
    this.workList = new ArrayDeque<Node>();
    Node nd;

    this.workList.add(this.nodes.get(0));

    while (this.workList.size() > 0) {
      nd = this.workList.removeLast();

      if (this.movements.contains(nd)) {
        // do nothing
      }
      else if (nd.equals(this.nodes.get(this.nodes.size() - 1))) {
        searchHelper(this.cameFromEdge, nd);
        this.workList.clear();
      }
      else {
        for (Edge cur : this.mst) {
          if ((nd.left.equals(cur.first) && nd.equals(cur.second))
              || (nd.left.equals(cur.second) && nd.equals(cur.first))) {
            this.workList.add(nd.left);

            if (!this.cameFromEdge.containsKey(nd.left)) {
              this.cameFromEdge.put(nd.left, new Edge(cur.second, cur.first, cur.weight));
            }

          }

          if ((nd.right.equals(cur.first) && nd.equals(cur.second))
              || ((nd.right.equals(cur.second)) && (nd.equals(cur.first)))) {
            this.workList.add(nd.right);

            if (!this.cameFromEdge.containsKey(nd.right)) {
              this.cameFromEdge.put(nd.right, cur);
            }

          }

          if ((nd.bottom.equals(cur.first) && nd.equals(cur.second))
              || (nd.bottom.equals(cur.second) && nd.equals(cur.first))) {
            this.workList.add(nd.bottom);

            if (!this.cameFromEdge.containsKey(nd.bottom)) {
              this.cameFromEdge.put(nd.bottom, cur);
            }

          }

          if (((nd.top.equals(cur.first)) && (nd.equals(cur.second)))
              || ((nd.top.equals(cur.second)) && (nd.equals(cur.first)))) {
            this.workList.add(nd.top);
            if (!this.cameFromEdge.containsKey(nd.top)) {
              this.cameFromEdge.put(nd.top, new Edge(cur.second, cur.first, cur.weight));
            }
          }

        }
        this.movements.add(nd);
      }
    }

    this.movements = tempMovements;
    return this.solution;
  }

  /**
   * returns a Posn that represents the distance between two nodes
   * 
   * @param n1 the first node
   * @param n2 the second node
   * @return the distance between the first and second node.
   */
  Posn distanceFromPosn(Node n1, Node n2) {
    int newX = Math.abs(n2.x - n1.x);
    int newY = Math.abs(n2.y - n1.y);
    return new Posn(newX, newY);
  }

  /**
   * helper function for the method search that adds the correct nodes to the
   * solution
   * 
   * @param cameFromEdge contains the nodes and edges that will be used to search
   * @param next         the next node to search.
   */
  void searchHelper(HashMap<Node, Edge> cameFromEdge, Node next) {
    this.solution.add(next);
    boolean temp = true;
    if (next.equals(this.nodes.get(0))) {
      temp = false;
    }

    else {
      searchHelper(cameFromEdge, cameFromEdge.get(next).first);
    }
  }

  /**
   * checks if the player reached the end of the maze
   */
  public WorldEnd worldEnds() {
    if (this.player.x == this.nodes.get(this.nodes.size() - 1).x
        && this.player.y == this.nodes.get(this.nodes.size() - 1).y) {

      this.solution = this.getSolution();

      this.makeFinalScene("You win!");
      return new WorldEnd(true, this.makeFinalScene("You win!"));
    }
    return new WorldEnd(false, this.makeScene());
  }

  /*
   * makes final scene visual.
   */
  public WorldScene makeFinalScene(String s) {
    WorldScene sc = this.makeScene();

    int nodeS = 1275 / (this.width + this.height);
    int tSize = nodeS;
    if (this.width + this.height > 60) {
      tSize *= 10;
    }
    WorldImage text = new TextImage(s, tSize, Color.orange);
    sc.placeImageXY(text, this.width * nodeS / 2, this.height * nodeS / 2);

    return sc;
  }

  /**
   *
   * onTick event function for bigbang searches for a path using breadth or depth
   * algorithm based on whether either the depthInit boolean or breadthInit
   * boolean is true Performs the type of search intended by the user
   */
  public void onTick() {
    boolean temp;
    if (this.depthInit || this.breadthInit) {
      Node nd = null;
      if (this.workList.size() > 0) {
        if (this.depthInit) {
          nd = this.workList.removeLast();
        }
        else if (this.breadthInit) {
          nd = this.workList.removeFirst();
        }

        if (this.movements.contains(nd)) {
          temp = true;
        }
        else if (nd.equals(this.nodes.get(this.nodes.size() - 1))) {
          searchHelper(this.cameFromEdge, nd);
          this.workList.clear();
        }
        else {
          for (Edge cur : this.mst) {
            if ((nd.left.equals(cur.first) && nd.equals(cur.second))
                || (nd.left.equals(cur.second) && nd.equals(cur.first))) {
              this.workList.add(nd.left);

              if (!this.cameFromEdge.containsKey(nd.left)) {
                this.cameFromEdge.put(nd.left, new Edge(cur.second, cur.first, cur.weight));
              }

            }

            if ((nd.right.equals(cur.first) && nd.equals(cur.second))
                || ((nd.right.equals(cur.second)) && (nd.equals(cur.first)))) {
              this.workList.add(nd.right);

              if (!this.cameFromEdge.containsKey(nd.right)) {
                this.cameFromEdge.put(nd.right, cur);
              }

            }

            if ((nd.bottom.equals(cur.first) && nd.equals(cur.second))
                || (nd.bottom.equals(cur.second) && nd.equals(cur.first))) {
              this.workList.add(nd.bottom);

              if (!this.cameFromEdge.containsKey(nd.bottom)) {
                this.cameFromEdge.put(nd.bottom, cur);
              }

            }

            if (((nd.top.equals(cur.first)) && (nd.equals(cur.second)))
                || ((nd.top.equals(cur.second)) && (nd.equals(cur.first)))) {
              this.workList.add(nd.top);
              if (!this.cameFromEdge.containsKey(nd.top)) {
                this.cameFromEdge.put(nd.top, new Edge(cur.second, cur.first, cur.weight));
              }
            }

          }
          this.movements.add(nd);
        }
      }
    }

  }

}

/**
 * Test class for Maze game. represents the examples for the maze
 *
 */
class MazeExamples {

  Maze m1 = new Maze(2, 2);
  Maze m3;
  Maze m4;
  Maze ms1;
  Maze ms2;

  Node n1;
  Node n2;
  Node n3;
  Node n4;
  Node n5;
  Node n6;
  Node n7;
  Node n8;
  Node n1a;
  Node n2a;
  Node n3a;
  Node n4a;

  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;
  Edge e5;
  Edge e6;
  Edge e7;
  Edge e8;
  Edge e9;
  Edge e10;
  Edge e1b;
  Edge e2b;
  Edge e3b;
  Edge e4b;

  Maze m2 = new Maze(10, 20);

  ArrayList<Node> aln1;
  ArrayList<Node> aln2;
  ArrayList<Node> aln3;
  ArrayList<Edge> ale1;
  ArrayList<Edge> ale2;
  ArrayList<Edge> ale3;
  ArrayList<Edge> ale4;

  Node a;
  Node b;
  Node c;
  Edge ee1;
  Edge ee2;
  Edge ee3;
  EdgeComparator edCom;

  void initData() {
    m3 = new Maze(2, 2, new Random(31));
    m4 = new Maze(2, 4, new Random(14));

    ms1 = new Maze(10, 10, new Random(31));
    ms2 = new Maze(10, 20, new Random(20));

    edCom = new EdgeComparator();

    a = new Node(0, 0);
    b = new Node(1, 0);
    c = new Node(0, 1);

    ee1 = new Edge(a, b, 5);
    ee2 = new Edge(a, c, 10);
    ee3 = new Edge(b, c, 8);

    n1 = new Node(0, 0);
    n2 = new Node(0, 1);
    n3 = new Node(1, 0);
    n4 = new Node(1, 1);

    n1a = new Node(0, 0);
    n2a = new Node(0, 1);
    n3a = new Node(1, 0);
    n4a = new Node(1, 1);

    n5 = new Node(2, 0);
    n6 = new Node(2, 1);
    n7 = new Node(3, 0);
    n8 = new Node(3, 1);

    this.n1.right = this.n3;
    this.n1.bottom = this.n2;
    this.n1.top = this.n1;
    this.n1.left = this.n1;
    this.n2.top = this.n1;
    this.n2.right = this.n4;
    this.n2.bottom = this.n2;
    this.n2.left = this.n2;
    this.n3.left = this.n1;
    this.n3.bottom = this.n4;
    this.n3.top = this.n3;
    this.n3.right = this.n3;
    this.n4.top = this.n3;
    this.n4.left = this.n2;
    this.n4.bottom = this.n4;
    this.n4.right = this.n4;

    e1 = new Edge(this.n1, this.n3, 233);
    e2 = new Edge(this.n1, this.n2, 157);
    e3 = new Edge(this.n2, this.n4, 585);
    e4 = new Edge(this.n3, this.n4, 471);
    e5 = new Edge(this.n3, this.n5, 402);
    e6 = new Edge(this.n4, this.n6, 509);
    e7 = new Edge(this.n5, this.n7, 217);
    e8 = new Edge(this.n5, this.n6, 974);
    e9 = new Edge(this.n6, this.n8, 726);
    e10 = new Edge(this.n7, this.n8, 788);
    e1b = new Edge(this.n1, this.n3, 616);
    e2b = new Edge(this.n1, this.n2, 885);
    e3b = new Edge(this.n2, this.n4, 568);
    e4b = new Edge(this.n3, this.n4, 499);

    aln1 = new ArrayList<Node>(Arrays.asList(this.n1, this.n2, this.n3, this.n4));
    aln2 = new ArrayList<Node>(
        Arrays.asList(this.n1, this.n2, this.n3, this.n4, this.n5, this.n6, this.n7, this.n8));
    aln3 = new ArrayList<Node>(Arrays.asList(this.n1a, this.n2a, this.n3a, this.n4a));
    ale1 = new ArrayList<Edge>(Arrays.asList(this.e1, this.e2, this.e3, this.e4));
    ale2 = new ArrayList<Edge>(Arrays.asList(this.e1b, this.e2b, this.e3b, this.e5, this.e4b,
        this.e6, this.e7, this.e8, this.e9, this.e10));
    ale3 = new ArrayList<Edge>(Arrays.asList(this.e7, this.e5, this.e4b, this.e6, this.e3b,
        this.e1b, this.e9, this.e10, this.e2b, this.e8));
    ale4 = new ArrayList<Edge>(Arrays.asList(this.e2, this.e1, this.e4, this.e3));

  }

  void testInitData(Tester t) {

    this.n1 = new Node(0, 0);
    this.n2 = new Node(0, 1);
    this.n3 = new Node(1, 0);
    this.n4 = new Node(1, 1);

    this.e1 = new Edge(this.n1, this.n3);
    this.e2 = new Edge(this.n1, this.n2);
    this.e3 = new Edge(this.n2, this.n4);
    this.e4 = new Edge(this.n3, this.n4);
    this.e5 = new Edge(this.n3, this.n5);
    this.e6 = new Edge(this.n4, this.n6);
    this.e7 = new Edge(this.n5, this.n7);
    this.e8 = new Edge(this.n5, this.n6);
    this.e9 = new Edge(this.n6, this.n8);
    this.e10 = new Edge(this.n7, this.n8);

    this.aln1 = new ArrayList<Node>(Arrays.asList());
    this.ale1 = new ArrayList<Edge>(Arrays.asList());

    t.checkExpect(this.n1.right, null);
    t.checkExpect(this.n1.bottom, null);
    t.checkExpect(this.n2.right, null);
    t.checkExpect(this.n2.top, null);
    t.checkExpect(this.n3.left, null);
    t.checkExpect(this.n3.bottom, null);
    t.checkExpect(this.n4.left, null);
    t.checkExpect(this.n4.top, null);

    t.checkExpect(this.e1.weight, 0);
    t.checkExpect(this.e2.weight, 0);
    t.checkExpect(this.e3.weight, 0);
    t.checkExpect(this.e4.weight, 0);
    t.checkExpect(this.e5.weight, 0);
    t.checkExpect(this.e6.weight, 0);
    t.checkExpect(this.e7.weight, 0);
    t.checkExpect(this.e8.weight, 0);
    t.checkExpect(this.e9.weight, 0);
    t.checkExpect(this.e10.weight, 0);

    t.checkExpect(this.aln1.isEmpty(), true);
    t.checkExpect(this.ale1.isEmpty(), true);

    initData();

    t.checkExpect(this.n1.right, this.n3);
    t.checkExpect(this.n1.bottom, this.n2);
    t.checkExpect(this.n2.right, this.n4);
    t.checkExpect(this.n2.top, this.n1);
    t.checkExpect(this.n3.left, this.n1);
    t.checkExpect(this.n3.bottom, this.n4);
    t.checkExpect(this.n4.left, this.n2);
    t.checkExpect(this.n4.top, this.n3);

    t.checkExpect(this.e1.weight, 233);
    t.checkExpect(this.e2.weight, 157);
    t.checkExpect(this.e3.weight, 585);
    t.checkExpect(this.e4.weight, 471);
    t.checkExpect(this.e5.weight, 402);
    t.checkExpect(this.e6.weight, 509);
    t.checkExpect(this.e7.weight, 217);
    t.checkExpect(this.e8.weight, 974);
    t.checkExpect(this.e9.weight, 726);
    t.checkExpect(this.e10.weight, 788);

    ArrayList<Node> temp = new ArrayList<Node>(Arrays.asList(this.n1, this.n2, this.n3, this.n4));
    ArrayList<Edge> temp1 = new ArrayList<Edge>(Arrays.asList(this.e1, this.e2, this.e3, this.e4));
    t.checkExpect(this.aln1.isEmpty(), false);
    t.checkExpect(this.ale1.isEmpty(), false);
    t.checkExpect(this.aln1, temp);
    t.checkExpect(this.ale1, temp1);
  }

  void testSetNode(Tester t) {
    initData();
    this.n1a.setNode(this.aln3, 0, 2, 2);
    t.checkExpect(this.n1.top, this.n1);
    t.checkExpect(this.n1.left, this.n1);
    t.checkExpect(this.n1.right, this.n3);
    t.checkExpect(this.n1.bottom, this.n2);

    this.n2a.setNode(this.aln3, 1, 2, 2);
    t.checkExpect(this.n2.top, this.n1);
    t.checkExpect(this.n2.left, this.n2);
    t.checkExpect(this.n2.right, this.n4);
    t.checkExpect(this.n2.bottom, this.n2);

    this.n3a.setNode(this.aln3, 2, 2, 2);
    t.checkExpect(this.n3.top, this.n3);
    t.checkExpect(this.n3.left, this.n1);
    t.checkExpect(this.n3.right, this.n3);
    t.checkExpect(this.n3.bottom, this.n4);

    this.n4a.setNode(this.aln3, 3, 2, 2);
    t.checkExpect(this.n4.top, this.n3);
    t.checkExpect(this.n4.left, this.n2);
    t.checkExpect(this.n4.right, this.n4);
    t.checkExpect(this.n4.bottom, this.n4);
  }

  void testSetMaze(Tester t) {
    initData();

    t.checkExpect(this.m3.nodes, this.aln1);
    t.checkExpect(this.m3.edges, this.ale1);

    this.n3.right = this.n5;
    this.n4.right = this.n6;

    this.n5.right = this.n7;
    this.n5.left = this.n3;
    this.n5.bottom = this.n6;
    this.n5.top = this.n5;

    this.n6.right = this.n8;
    this.n6.left = this.n4;
    this.n6.top = this.n5;
    this.n6.bottom = this.n6;

    this.n7.left = this.n5;
    this.n7.bottom = this.n8;
    this.n7.right = this.n7;
    this.n7.top = this.n7;

    this.n8.top = this.n7;
    this.n8.left = this.n6;
    this.n8.bottom = this.n8;
    this.n8.right = this.n8;

    t.checkExpect(this.m4.nodes, this.aln2);
    t.checkExpect(this.m4.edges, this.ale2);
  }

  void testSortEdges(Tester t) {
    initData();
    this.m3.sortEdges();
    t.checkExpect(this.m3.edges, this.ale4);

    this.n3.right = this.n5;
    this.n4.right = this.n6;

    this.n5.right = this.n7;
    this.n5.left = this.n3;
    this.n5.bottom = this.n6;
    this.n5.top = this.n5;

    this.n6.right = this.n8;
    this.n6.left = this.n4;
    this.n6.top = this.n5;
    this.n6.bottom = this.n6;

    this.n7.left = this.n5;
    this.n7.bottom = this.n8;
    this.n7.right = this.n7;
    this.n7.top = this.n7;

    this.n8.top = this.n7;
    this.n8.left = this.n6;
    this.n8.bottom = this.n8;
    this.n8.right = this.n8;

    this.m4.sortEdges();
    t.checkExpect(this.m4.edges, this.ale3);
  }

  void testCompare(Tester t) {
    this.initData();
    t.checkExpect(this.edCom.compare(ee1, ee2), -5);
    t.checkExpect(this.edCom.compare(ee2, ee1), 5);
    t.checkExpect(this.edCom.compare(ee2, ee3), 2);
    t.checkExpect(this.edCom.compare(ee3, ee2), -2);

  }

  void testCreateMST(Tester t) {
    initData();
    ArrayList<Edge> temp = new ArrayList<Edge>(Arrays.asList(this.e2, this.e1, this.e4));
    HashMap<Posn, Posn> map = new HashMap<Posn, Posn>();
    map.put(new Posn(0, 0), new Posn(0, 1));
    map.put(new Posn(1, 0), new Posn(1, 1));
    map.put(new Posn(0, 1), new Posn(1, 0));
    map.put(new Posn(1, 1), new Posn(1, 1));

    this.m3.sortEdges();
    this.m3.createMST();
    t.checkExpect(this.m3.mst, temp);
    t.checkExpect(this.m3.map, map);
    this.m3.edges.removeAll(this.m3.mst);
    t.checkExpect(this.m3.edges, new ArrayList<Edge>(Arrays.asList(this.e3)));

    initData();

    this.n3.right = this.n5;
    this.n4.right = this.n6;

    this.n5.right = this.n7;
    this.n5.left = this.n3;
    this.n5.bottom = this.n6;
    this.n5.top = this.n5;

    this.n6.right = this.n8;
    this.n6.left = this.n4;
    this.n6.top = this.n5;
    this.n6.bottom = this.n6;

    this.n7.left = this.n5;
    this.n7.bottom = this.n8;
    this.n7.right = this.n7;
    this.n7.top = this.n7;

    this.n8.top = this.n7;
    this.n8.left = this.n6;
    this.n8.bottom = this.n8;
    this.n8.right = this.n8;

    ArrayList<Edge> temp2 = new ArrayList<Edge>(
        Arrays.asList(this.e7, this.e5, this.e4b, this.e6, this.e3b, this.e1b, this.e9));
    HashMap<Posn, Posn> map2 = new HashMap<Posn, Posn>();
    map2.put(new Posn(0, 0), new Posn(2, 1));
    map2.put(new Posn(1, 0), new Posn(3, 0));
    map2.put(new Posn(2, 0), new Posn(3, 0));
    map2.put(new Posn(3, 0), new Posn(1, 1));
    map2.put(new Posn(0, 1), new Posn(2, 1));
    map2.put(new Posn(1, 1), new Posn(2, 1));
    map2.put(new Posn(2, 1), new Posn(3, 1));
    map2.put(new Posn(3, 1), new Posn(3, 1));

    this.m4.sortEdges();
    this.m4.createMST();
    t.checkExpect(this.m4.mst, temp2);
    t.checkExpect(this.m4.map, map2);
    this.m4.edges.removeAll(this.m4.mst);
    t.checkExpect(this.m4.edges, new ArrayList<Edge>(Arrays.asList(this.e10, this.e2b, this.e8)));
  }

  void testFind(Tester t) {
    initData();
    Maze temp = new Maze(2, 2, new Random(213));
    HashMap<Posn, Posn> map = new HashMap<Posn, Posn>();
    temp.map = map;
    map.put(new Posn(0, 0), new Posn(0, 0));
    map.put(new Posn(1, 0), new Posn(1, 0));
    map.put(new Posn(0, 1), new Posn(0, 1));
    map.put(new Posn(1, 1), new Posn(1, 1));
    map.put(new Posn(2, 1), new Posn(0, 0));
    map.put(new Posn(3, 1), new Posn(2, 1));

    t.checkExpect(temp.find(new Posn(0, 0)), new Posn(0, 0));
    t.checkExpect(temp.find(new Posn(1, 0)), new Posn(1, 0));
    t.checkExpect(temp.find(new Posn(0, 1)), new Posn(0, 1));
    t.checkExpect(temp.find(new Posn(1, 1)), new Posn(1, 1));
    t.checkExpect(temp.find(new Posn(2, 1)), new Posn(0, 0));
    t.checkExpect(temp.find(new Posn(3, 1)), new Posn(0, 0));
  }

  void testDistanceFromPosn(Tester t) {
    initData();
    t.checkExpect(this.m1.distanceFromPosn(this.n1, this.n2), new Posn(0, 1));
    t.checkExpect(this.m1.distanceFromPosn(this.n1, this.n1), new Posn(0, 0));
    t.checkExpect(this.m1.distanceFromPosn(this.n4, this.n4), new Posn(0, 0));
    t.checkExpect(this.m1.distanceFromPosn(this.n1, this.n3), new Posn(1, 0));
    t.checkExpect(this.m1.distanceFromPosn(this.n4, this.n2), new Posn(1, 0));
    t.checkExpect(this.m1.distanceFromPosn(this.n1, this.n4), new Posn(1, 1));
    t.checkExpect(this.m1.distanceFromPosn(this.n4, this.n1), new Posn(1, 1));
  }

  void testGetSolution(Tester t) {
    initData();

    Maze temp1 = new Maze(2, 2, new Random(5371));

    temp1.sortEdges();
    temp1.createMST();
    temp1.edges.removeAll(temp1.mst);

    t.checkExpect(temp1.getSolution(),
        new ArrayList<Node>(Arrays.asList(this.n4, this.n3, this.n1)));

    Maze temp = new Maze(2, 3, new Random(60));

    temp.sortEdges();
    temp.createMST();
    temp.edges.removeAll(temp.mst);

    this.n3.right = this.n5;
    this.n4.right = this.n6;

    this.n5.right = this.n5;
    this.n5.left = this.n3;
    this.n5.bottom = this.n6;
    this.n5.top = this.n5;

    this.n6.right = this.n6;
    this.n6.left = this.n4;
    this.n6.top = this.n5;
    this.n6.bottom = this.n6;

    t.checkExpect(temp.getSolution(),
        new ArrayList<Node>(Arrays.asList(this.n6, this.n4, this.n2, this.n1)));

    this.m4.sortEdges();
    this.m4.createMST();
    this.m4.edges.removeAll(this.m4.mst);

    this.n3.right = this.n5;
    this.n4.right = this.n6;

    this.n5.right = this.n7;
    this.n5.left = this.n3;
    this.n5.bottom = this.n6;
    this.n5.top = this.n5;

    this.n6.right = this.n8;
    this.n6.left = this.n4;
    this.n6.top = this.n5;
    this.n6.bottom = this.n6;

    this.n7.left = this.n5;
    this.n7.bottom = this.n8;
    this.n7.right = this.n7;
    this.n7.top = this.n7;

    this.n8.top = this.n7;
    this.n8.left = this.n6;
    this.n8.bottom = this.n8;
    this.n8.right = this.n8;

    t.checkExpect(this.m4.getSolution(),
        new ArrayList<Node>(Arrays.asList(this.n8, this.n6, this.n4, this.n3, this.n1)));

  }

  void testSearchHelper(Tester t) {
    initData();

    Maze temp = new Maze(2, 2, new Random(412));
    temp.sortEdges();
    temp.createMST();
    temp.edges.removeAll(temp.mst);
    temp.depthInit = true;
    temp.player = temp.nodes.get(0);
    temp.workList.add(temp.nodes.get(0));

    t.checkExpect(temp.cameFromEdge, new HashMap<Node, Edge>());
    t.checkExpect(temp.solution, new ArrayList<Node>());

    temp.onTick();
    temp.onTick();
    temp.onTick();

    temp.searchHelper(temp.cameFromEdge, temp.nodes.get(3));

    t.checkExpect(temp.solution, new ArrayList<Node>(Arrays.asList(this.n4, this.n2, this.n1)));

    Maze temp1 = new Maze(2, 2, new Random(112));
    temp1.sortEdges();
    temp1.createMST();
    temp1.edges.removeAll(temp1.mst);
    temp1.depthInit = true;
    temp1.player = temp1.nodes.get(0);
    temp1.workList.add(temp1.nodes.get(0));

    t.checkExpect(temp1.cameFromEdge, new HashMap<Node, Edge>());
    t.checkExpect(temp1.solution, new ArrayList<Node>());

    temp1.onTick();
    temp1.onTick();
    temp1.onTick();

    temp1.searchHelper(temp1.cameFromEdge, temp1.nodes.get(3));

    t.checkExpect(temp1.solution, new ArrayList<Node>(Arrays.asList(this.n4, this.n3, this.n1)));

  }

  void testOnTick(Tester t) {
    initData();

    Maze temp = new Maze(2, 2, new Random(103));
    temp.sortEdges();
    temp.createMST();
    temp.edges.removeAll(temp.mst);
    temp.player = temp.nodes.get(0);
    temp.workList.add(temp.nodes.get(0));

    t.checkExpect(temp.cameFromEdge, new HashMap<Node, Edge>());
    t.checkExpect(temp.solution, new ArrayList<Node>());
    t.checkExpect(temp.movements, new ArrayList<Node>());

    temp.depthInit = true;
    temp.onTick();
    t.checkExpect(temp.movements, new ArrayList<Node>(Arrays.asList(this.n1)));

    temp.onTick();

    temp.onTick();
    t.checkExpect(temp.movements, new ArrayList<Node>(Arrays.asList(this.n1, this.n3)));

    temp.onTick();
    t.checkExpect(temp.solution, new ArrayList<Node>(Arrays.asList(this.n4, this.n3, this.n1)));

    Maze temp2 = new Maze(2, 4, new Random(1033));
    temp2.sortEdges();
    temp2.createMST();
    temp2.edges.removeAll(temp2.mst);
    temp2.player = temp2.nodes.get(0);
    temp2.workList.add(temp2.nodes.get(0));

    this.n3.right = this.n5;
    this.n4.right = this.n6;

    this.n5.right = this.n7;
    this.n5.left = this.n3;
    this.n5.bottom = this.n6;
    this.n5.top = this.n5;

    this.n6.right = this.n8;
    this.n6.left = this.n4;
    this.n6.top = this.n5;
    this.n6.bottom = this.n6;

    this.n7.left = this.n5;
    this.n7.bottom = this.n8;
    this.n7.right = this.n7;
    this.n7.top = this.n7;

    this.n8.top = this.n7;
    this.n8.left = this.n6;
    this.n8.bottom = this.n8;
    this.n8.right = this.n8;

    t.checkExpect(temp2.cameFromEdge, new HashMap<Node, Edge>());
    t.checkExpect(temp2.solution, new ArrayList<Node>());
    t.checkExpect(temp2.movements, new ArrayList<Node>());

    temp2.breadthInit = true;
    temp2.onTick();
    t.checkExpect(temp2.movements, new ArrayList<Node>(Arrays.asList(this.n1)));

    temp2.onTick();
    t.checkExpect(temp2.movements, new ArrayList<Node>(Arrays.asList(this.n1, this.n2)));

    temp2.onTick();
    t.checkExpect(temp2.movements, new ArrayList<Node>(Arrays.asList(this.n1, this.n2, this.n3)));

    temp2.onTick();
    t.checkExpect(temp2.movements,
        new ArrayList<Node>(Arrays.asList(this.n1, this.n2, this.n3, this.n4)));

    temp2.onTick();
    temp2.onTick();

    temp2.onTick();
    t.checkExpect(temp2.movements,
        new ArrayList<Node>(Arrays.asList(this.n1, this.n2, this.n3, this.n4, this.n6)));

    temp2.onTick();
    temp2.onTick();

    temp2.onTick();
    t.checkExpect(temp2.movements,
        new ArrayList<Node>(Arrays.asList(this.n1, this.n2, this.n3, this.n4, this.n6, this.n5)));

    temp2.onTick();
    t.checkExpect(temp2.movements, new ArrayList<Node>(
        Arrays.asList(this.n1, this.n2, this.n3, this.n4, this.n6, this.n5, this.n7)));

    temp2.onTick();
    temp2.onTick();
    temp2.onTick();

    t.checkExpect(temp2.solution, new ArrayList<Node>(
        Arrays.asList(this.n8, this.n7, this.n5, this.n6, this.n4, this.n2, this.n1)));
  }

  void testOnKeyEvent(Tester t) {
    initData();

    Maze temp = new Maze(2, 2, new Random(103));
    temp.sortEdges();
    temp.createMST();
    temp.edges.removeAll(temp.mst);
    temp.player = temp.nodes.get(0);
    temp.workList.add(temp.nodes.get(0));

    t.checkExpect(temp.movements, new ArrayList<Node>());
    temp.onKeyEvent("right");
    t.checkExpect(temp.movements, new ArrayList<Node>(Arrays.asList(this.n1)));
    temp.onKeyEvent("down");
    t.checkExpect(temp.movements, new ArrayList<Node>(Arrays.asList(this.n1, this.n3)));

    temp.player = temp.nodes.get(3);
    temp.movements = new ArrayList<Node>();

    temp.onKeyEvent("up");
    t.checkExpect(temp.movements, new ArrayList<Node>(Arrays.asList(this.n4)));
    temp.onKeyEvent("left");
    t.checkExpect(temp.movements, new ArrayList<Node>(Arrays.asList(this.n4, this.n3)));

    t.checkExpect(temp.depthInit, false);
    temp.onKeyEvent("d");
    t.checkExpect(temp.depthInit, true);
    temp.onKeyEvent("d");
    t.checkExpect(temp.depthInit, false);

    t.checkExpect(temp.breadthInit, false);
    temp.onKeyEvent("b");
    t.checkExpect(temp.breadthInit, true);
    temp.onKeyEvent("b");
    t.checkExpect(temp.breadthInit, false);

    t.checkExpect(temp.movements, new ArrayList<Node>(Arrays.asList(this.n4, this.n3)));
    temp.player = temp.nodes.get(3);
    t.checkExpect(temp.player, this.n4);

    ArrayList<Node> nodesPrev = temp.nodes;
    ArrayList<Edge> edgesPrev = temp.edges;

    temp.onKeyEvent("r");

    t.checkExpect(temp.player, this.n1);
    t.checkExpect(temp.cameFromEdge, new HashMap<Node, Edge>());
    t.checkExpect(temp.solution, new ArrayList<Node>());
    t.checkExpect(temp.movements, new ArrayList<Node>());
    t.checkExpect(temp.edges.size(), edgesPrev.size());

    for (int i = 0; i < temp.nodes.size(); i++) {
      t.checkExpect(temp.nodes.get(i).x, nodesPrev.get(i).x);
      t.checkExpect(temp.nodes.get(i).y, nodesPrev.get(i).y);
    }

    Maze temp2 = new Maze(2, 4, new Random(1033));
    temp2.sortEdges();
    temp2.createMST();
    temp2.edges.removeAll(temp2.mst);
    temp2.player = temp2.nodes.get(0);
    temp2.workList.add(temp2.nodes.get(0));

    this.n3.right = this.n5;
    this.n4.right = this.n6;

    this.n5.right = this.n7;
    this.n5.left = this.n3;
    this.n5.bottom = this.n6;
    this.n5.top = this.n5;

    this.n6.right = this.n8;
    this.n6.left = this.n4;
    this.n6.top = this.n5;
    this.n6.bottom = this.n6;

    this.n7.left = this.n5;
    this.n7.bottom = this.n8;
    this.n7.right = this.n7;
    this.n7.top = this.n7;

    this.n8.top = this.n7;
    this.n8.left = this.n6;
    this.n8.bottom = this.n8;
    this.n8.right = this.n8;

    temp2.player = temp2.nodes.get(4);

    t.checkExpect(temp2.movements, new ArrayList<Node>());

    temp2.onKeyEvent("right");
    t.checkExpect(temp2.movements, new ArrayList<Node>(Arrays.asList(this.n5)));

    temp2.onKeyEvent("down");
    t.checkExpect(temp2.movements, new ArrayList<Node>(Arrays.asList(this.n5, this.n7)));

    temp2.player = temp2.nodes.get(7);
    temp2.movements = new ArrayList<Node>();

    temp2.onKeyEvent("up");
    t.checkExpect(temp2.movements, new ArrayList<Node>(Arrays.asList(this.n8)));

    temp2.onKeyEvent("left");
    t.checkExpect(temp2.movements, new ArrayList<Node>(Arrays.asList(this.n8, this.n7)));

    t.checkExpect(temp2.depthInit, false);
    temp2.onKeyEvent("d");
    t.checkExpect(temp2.depthInit, true);
    temp2.onKeyEvent("d");
    t.checkExpect(temp2.depthInit, false);

    t.checkExpect(temp2.breadthInit, false);
    temp2.onKeyEvent("b");
    t.checkExpect(temp2.breadthInit, true);
    temp2.onKeyEvent("b");
    t.checkExpect(temp2.breadthInit, false);

    t.checkExpect(temp2.movements, new ArrayList<Node>(Arrays.asList(this.n8, this.n7)));
    temp2.player = temp2.nodes.get(5);
    t.checkExpect(temp2.player, this.n6);

    ArrayList<Node> nodesPrev1 = temp2.nodes;
    ArrayList<Edge> edgesPrev1 = temp2.edges;

    temp2.onKeyEvent("r");

    t.checkExpect(temp2.player, this.n1);
    t.checkExpect(temp2.cameFromEdge, new HashMap<Node, Edge>());
    t.checkExpect(temp2.solution, new ArrayList<Node>());
    t.checkExpect(temp2.movements, new ArrayList<Node>());
    t.checkExpect(temp2.edges.size(), edgesPrev1.size());

    for (int i = 0; i < temp2.nodes.size(); i++) {
      t.checkExpect(temp2.nodes.get(i).x, nodesPrev1.get(i).x);
      t.checkExpect(temp2.nodes.get(i).y, nodesPrev1.get(i).y);
    }
  }

  void testWorldEnds(Tester t) {
    initData();

    t.checkExpect(this.m1.worldEnds(), new WorldEnd(false, this.m1.makeScene()));

    this.m1.player = this.m1.nodes.get(2);
    t.checkExpect(this.m1.worldEnds(), new WorldEnd(false, this.m1.makeScene()));

    this.m1.player = this.m1.nodes.get(3);
    t.checkExpect(this.m1.worldEnds(), new WorldEnd(true, this.m1.makeFinalScene("You win!")));

    t.checkExpect(this.ms1.worldEnds(), new WorldEnd(false, this.ms1.makeScene()));

    this.ms1.player = this.ms1.nodes.get(59);
    t.checkExpect(this.ms1.worldEnds(), new WorldEnd(false, this.ms1.makeScene()));

  }

  void testMakeFinalScene(Tester t) {
    initData();

    WorldScene sc = this.m1.makeScene();

    int nodeS = 1275 / (this.m1.width + this.m1.height);
    int tSize = nodeS;
    if (this.m1.width + this.m1.height > 60) {
      tSize *= 10;
    }
    WorldImage text = new TextImage("You Win!", tSize, Color.orange);
    sc.placeImageXY(text, this.m1.width * nodeS / 2, this.m1.height * nodeS / 2);

    t.checkExpect(this.m1.makeFinalScene("You Win!"), sc);

    WorldScene sc1 = this.ms1.makeScene();

    int nodeS1 = 1275 / (this.ms1.width + this.ms1.height);
    int tSize1 = nodeS1;
    if (this.ms1.width + this.ms1.height > 60) {
      tSize1 *= 10;
    }
    WorldImage text1 = new TextImage("You Win!", tSize1, Color.orange);
    sc1.placeImageXY(text1, this.ms1.width * nodeS1 / 2, this.ms1.height * nodeS1 / 2);

    t.checkExpect(this.ms1.makeFinalScene("You Win!"), sc1);

    WorldScene sc2 = this.ms2.makeScene();

    int nodeS2 = 1275 / (this.ms2.width + this.ms2.height);
    int tSize2 = nodeS2;
    if (this.ms2.width + this.ms2.height > 60) {
      tSize2 *= 10;
    }
    WorldImage text2 = new TextImage("You Win!", tSize2, Color.orange);
    sc2.placeImageXY(text2, this.ms2.width * nodeS2 / 2, this.ms2.height * nodeS2 / 2);

    t.checkExpect(this.ms2.makeFinalScene("You Win!"), sc2);
  }

  void testMakeScene(Tester t) {
    initData();

    int nodeS = 1275 / (this.ms1.width + this.ms1.height);

    WorldScene sc1 = new WorldScene(this.ms1.width * nodeS * 2, this.ms1.height * nodeS * 2);

    WorldImage background = // new RectangleImage(1280, 1280, "solid", Color.LIGHT_GRAY);
        new RectangleImage(this.ms1.width * nodeS * 2, this.ms1.height * nodeS * 2, "solid",
            Color.LIGHT_GRAY);

    WorldImage tl = new RectangleImage(nodeS, nodeS, "solid", Color.green);
    WorldImage br = new RectangleImage(nodeS, nodeS, "solid", Color.magenta);
    sc1.placeImageXY(background, 0, 0);

    sc1.placeImageXY(tl, nodeS / 2, nodeS / 2);
    sc1.placeImageXY(br, nodeS * this.ms1.width - nodeS / 2, nodeS * this.ms1.height - nodeS / 2);

    for (int i = 0; i < this.ms1.edges.size(); i++) {
      Edge cur = this.ms1.edges.get(i);

      if (new Posn(0, 1).equals(this.ms1.distanceFromPosn(cur.first, cur.second))) {
        WorldImage line = new RectangleImage(nodeS, 1, "solid", Color.black);
        sc1.placeImageXY(line, nodeS * cur.second.x + nodeS / 2, nodeS * cur.second.y);
      }
      else if (new Posn(1, 0).equals(this.ms1.distanceFromPosn(cur.first, cur.second))) {
        WorldImage line = new RectangleImage(1, nodeS, "solid", Color.black);
        sc1.placeImageXY(line, nodeS * cur.second.x, nodeS * cur.second.y + nodeS / 2);
      }
    }

    for (int i = 0; i < this.ms1.movements.size(); i++) {
      WorldImage node = new RectangleImage(nodeS - 1, nodeS - 1, "solid", Color.cyan);
      sc1.placeImageXY(node, nodeS * this.ms1.movements.get(i).x + nodeS / 2,
          nodeS * this.ms1.movements.get(i).y + nodeS / 2);

    }

    for (int i = 0; i < this.ms1.solution.size(); i++) {
      WorldImage node = new RectangleImage(nodeS - 1, nodeS - 1, "solid", Color.blue);
      sc1.placeImageXY(node, nodeS * this.ms1.solution.get(i).x + nodeS / 2,
          nodeS * this.ms1.solution.get(i).y + nodeS / 2);

    }
    WorldImage playerN = new RectangleImage(nodeS - 1, nodeS - 1, "solid", Color.black);
    sc1.placeImageXY(playerN, nodeS * this.ms1.player.x + nodeS / 2,
        nodeS * this.ms1.player.y + nodeS / 2);

    t.checkExpect(this.ms1.makeScene(), sc1);

    int nodeS1 = 1275 / (this.ms2.width + this.ms2.height);

    WorldScene sc2 = new WorldScene(this.ms2.width * nodeS1 * 2, this.ms2.height * nodeS1 * 2);

    WorldImage background2 = // new RectangleImage(1280, 1280, "solid", Color.LIGHT_GRAY);
        new RectangleImage(this.ms2.width * nodeS1 * 2, this.ms2.height * nodeS1 * 2, "solid",
            Color.LIGHT_GRAY);

    WorldImage tl2 = new RectangleImage(nodeS1, nodeS1, "solid", Color.green);
    WorldImage br2 = new RectangleImage(nodeS1, nodeS1, "solid", Color.magenta);
    sc2.placeImageXY(background2, 0, 0);

    sc2.placeImageXY(tl2, nodeS1 / 2, nodeS1 / 2);
    sc2.placeImageXY(br2, nodeS1 * this.ms2.width - nodeS1 / 2,
        nodeS1 * this.ms2.height - nodeS1 / 2);

    for (int i = 0; i < this.ms2.edges.size(); i++) {
      Edge cur = this.ms2.edges.get(i);

      if (new Posn(0, 1).equals(this.ms2.distanceFromPosn(cur.first, cur.second))) {
        WorldImage line = new RectangleImage(nodeS1, 1, "solid", Color.black);
        sc2.placeImageXY(line, nodeS1 * cur.second.x + nodeS1 / 2, nodeS1 * cur.second.y);
      }
      else if (new Posn(1, 0).equals(this.ms2.distanceFromPosn(cur.first, cur.second))) {
        WorldImage line = new RectangleImage(1, nodeS1, "solid", Color.black);
        sc2.placeImageXY(line, nodeS1 * cur.second.x, nodeS1 * cur.second.y + nodeS1 / 2);
      }
    }

    for (int i = 0; i < this.ms2.movements.size(); i++) {
      WorldImage node = new RectangleImage(nodeS1 - 1, nodeS1 - 1, "solid", Color.cyan);
      sc2.placeImageXY(node, nodeS1 * this.ms2.movements.get(i).x + nodeS1 / 2,
          nodeS1 * this.ms2.movements.get(i).y + nodeS1 / 2);

    }

    for (int i = 0; i < this.ms2.solution.size(); i++) {
      WorldImage node = new RectangleImage(nodeS1 - 1, nodeS1 - 1, "solid", Color.blue);
      sc2.placeImageXY(node, nodeS1 * this.ms2.solution.get(i).x + nodeS1 / 2,
          nodeS1 * this.ms2.solution.get(i).y + nodeS1 / 2);

    }
    WorldImage playerN2 = new RectangleImage(nodeS1 - 1, nodeS1 - 1, "solid", Color.black);
    sc2.placeImageXY(playerN2, nodeS1 * this.ms2.player.x + nodeS1 / 2,
        nodeS1 * this.ms2.player.y + nodeS1 / 2);

    t.checkExpect(this.ms2.makeScene(), sc2);

  }

  // You can adjust the size of the maze by changing the dimensions of m2 inside
  // the method
  // 10 is the length and 20 is the width of the maze
  // You can adjust the numbers however way you want it
  void testBigBang(Tester t) {
    Maze m2 = new Maze(10, 20);
    int nodeS = 1275 / (m2.width + m2.height);
    m2.bigBang(m2.width * nodeS, m2.height * nodeS, .000001);
  }
}
