package game;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 An immutable representation of a tetris piece in a particular rotation.
 Each piece is defined by the blocks that make up its body.

 Typical client looks like...
 <pre>
 Piece[] pieces = Piece.getPieces();	// the array of base pieces
 Piece p = pieces[0];					// get piece 0
 int width = p.getWidth();				// get its width
 Piece next = p.nextRotation();			// get the next rotation of piece 0
 </pre>
 
 @author	Nick Parlante
*/
public final class Piece {
/*
 Implementation notes:
 -The starter code specs out a few simple things, but leaves  the key algorithms for you.
 -Store the body as a Point[] array
 -The ivars in the Point class are .x and .y
 -Do not assume there are 4 points in the body -- use array.length to keep the code general
*/
	private Point[] body;
	private int[] skirt;
	private int width;
	private int height;
	private Piece next;	// "next" rotation

	static private Piece[] pieces;	// singleton array of first rotations

	/**
	 Defines a new piece given the Points that make up its body.
	 Makes its own copy of the array and the Points inside it.
	 Does not set up the rotations.
	*/
	private Piece(Point[] points) {
            body = new Point[points.length];
            for(int i = 0; i < points.length; i++) {
                body[i] = new Point();
                body[i] = points[i];
            }
	}	

	/**
	 Returns the width of the piece measured in blocks.
	*/
	public int getWidth() {
		return(width);
	}

	/**
	 Returns the height of the piece measured in blocks.
	*/
	public int getHeight() {
		return(height);
	}

	/**
	 Returns a pointer to the piece's body. The caller
	 should not modify this array.
	*/
	public Point[] getBody() {
		return(body);
	}

	/**
	 Returns a pointer to the piece's skirt. For each x value
	 across the piece, the skirt gives the lowest y value in the body.
	 This useful for computing where the piece will land.
	*/
	public int[] getSkirt() {
		return(skirt);
	}

	/**
	 Returns a piece that is 90 degrees counter-clockwise rotated from the receiver.
	 
	 <p>Implementation:
	 The Piece class pre-computes all the rotations once.
	 This method just hops from one pre-computed rotation to the next in constant time.
	*/	
	public Piece nextRotation() {
		return next;
	}

	/**
	 Returns true if two pieces are the same -- their bodies contain the same points.
	 Interestingly, this is not the same as having exactly the same body arrays, since the points may not be
	 in the same order in the bodies.
	 Used internally to detect if two rotations are effectively the same.
	*/
	public boolean equals(Object obj) {
		if (obj == this) {
			return(true);
		}
		
		if (!(obj instanceof Piece)) {
			return (false);
		}
		
                if (((Piece)obj).body.length != this.body.length) {
					return false;
				}
                
                Collection setA = new HashSet();
                Collection setB = new HashSet();
                
                for(int i = 0; i < this.body.length; i++) {
                    setA.add(((Piece)obj).body[i]);
                    setB.add(this.body[i]);
                }

                boolean res = setA.equals(setB);
                return setA.equals(setB);
	}

	/**
	 Returns an array containing the first rotation of each of the 7 standard tetris pieces.
	 The next (counterclockwise) rotation can be obtained from each piece with the {@link #nextRotation()} message.
	*/
	public static Piece[] getPieces() {
		if (pieces==null) {
			pieces = new Piece[] {
				pieceRow(new Piece(parsePoints("0 0	0 1	0 2	0 3"))),	// 0
				pieceRow(new Piece(parsePoints("0 0	0 1	0 2	1 0"))),	// 1
				pieceRow(new Piece(parsePoints("0 0	1 0	1 1	1 2"))),	// 2
				pieceRow(new Piece(parsePoints("0 0	1 0	1 1	2 1"))),	// 3
				pieceRow(new Piece(parsePoints("0 1	1 1	1 0	2 0"))),	// 4
				pieceRow(new Piece(parsePoints("0 0	0 1	1 0	1 1"))),	// 5
				pieceRow(new Piece(parsePoints("0 0	1 0	1 1	2 0"))),	// 6
			};
		}
		
		return(pieces);
	}

	/**
	 Given a string of x,y pairs ("0 0	0 1	0 2	1 0"), parses the points into a Point[] array.
	*/
	private static Point[] parsePoints(String string) {
	    // could use Arraylist here, but use vector so works on Java 1.1
		Vector points = new Vector();
		StringTokenizer tok = new StringTokenizer(string);
		try {
			while(tok.hasMoreTokens()) {
				int x = Integer.parseInt(tok.nextToken());
				int y = Integer.parseInt(tok.nextToken());
				
				points.addElement(new Point(x, y));
			}
		}
		catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse x,y string:" + string);	// cheap way to do assert
		}
		
		// Make an array out of the Vector
		Point[] array = new Point[points.size()];
		points.copyInto(array);
		return(array);
	}

	/**
	 Given the "first" rotation of a piece piece, computes all the other rotations and links them all together
	 by their next pointers. Returns the first piece.
	 {@link nextRotation()} relies on the next pointers to get from one rotation to the next.
	 Internally, uses Piece.equals() to detect when the rotations have gotten us back to the first piece.
	*/
	private static Piece pieceRow(Piece root) {
		Piece temp = root;
		Piece prev = root;

		for(;;) {
			prev = temp;
			prev.setPieceDims();
			prev.setPieceSkirt();
			temp = new Piece(prev.body);
			temp = temp.rotatePiece();

			if(!temp.equals(root)) {
				prev.next = temp;
			} else {
				prev.next = root;
				break;
			}
		}

		return root;
	}
        
        private Piece rotatePiece() {
            Piece piece = null;
            Point[] temp = new Point[body.length];
            // switch x,y to y,x
            for(int i = 0; i < body.length; i++) {
                temp[i] = new Point();
                temp[i].x = body[i].y;
                temp[i].y = body[i].x;
            }

            piece = new Piece(temp);
            piece.setPieceDims();
            
            for(int i = 0; i < piece.body.length; i++) {
                temp[i].x = (piece.width-1) - piece.body[i].x;
                temp[i].y = piece.body[i].y;
            }

            piece = new Piece(temp);
            return(piece);
        }

        private void setPieceDims() {
            int maxWidth = -1;
            int maxHeight = -1;
            for(int i = 0; i < body.length; i++){
                if(body[i].x > maxWidth) maxWidth = body[i].x;
                if(body[i].y > maxHeight) maxHeight = body[i].y;
            }

            width = maxWidth+1;
            height = maxHeight+1;
        }
        
        private void setPieceSkirt() {
            int maxWidth = width;
            int maxHeight;
            
            skirt = new int[maxWidth];
            
            for(int i = 0; i < maxWidth; i++) {
                Point currentPoint = null;
                maxHeight = 10000;

                for(int j = 0; j < body.length; j++) {
                    if(body[j].x == i) {
                        if(body[j].y < maxHeight) {
                            maxHeight = body[j].y;
                            currentPoint = body[j];
                        }
                    }
                }

                skirt[i] = currentPoint.y;
            }
        }
}