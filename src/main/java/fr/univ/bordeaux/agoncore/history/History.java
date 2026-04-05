package fr.univ.bordeaux.agoncore.history;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.technical.utils.GameLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Manages the history of turns to support undo and redo operations.
 */
public class History {

  /** Stack containing turns that can be reverted. */
  private final Stack<HistoryInformations> undoStack = new Stack<>();

  /** Stack containing turns that were reverted and can be re-applied. */
  private final Stack<HistoryInformations> redoStack = new Stack<>();

  /** Initializes an empty game history. */
  public History() {
    GameLogger.debug("History: New empty history initialized.");
  }

  /**
   * Reconstructs the move history from a list of ABA-pro strings.
   *
   * @param textMoves The list of moves parsed from the save file.
   */
  public History(List<String> textMoves) {
    GameLogger.debug("History: Reconstructing history from " + textMoves.size() + " lines.");
    for (String textMove : textMoves) {
      String cleanMove = textMove.trim();
      if (cleanMove.isEmpty()) {
        continue;
      }

      String mainPart = cleanMove;
      String capturePart = null;

      int openParen = cleanMove.indexOf('(');
      int closeParen = cleanMove.indexOf(')');

      if (openParen != -1 && closeParen != -1 && closeParen > openParen) {
        mainPart = cleanMove.substring(0, openParen).trim();
        capturePart = cleanMove.substring(openParen + 1, closeParen).trim();
      }

      String[] parts = mainPart.split("\\s+");

      if (parts.length >= 3) {
        char pieceChar = parts[0].charAt(0);
        int fromIdx = CoordinateMapper.fromCoordinateString(parts[1]);
        int toIdx = CoordinateMapper.fromCoordinateString(parts[2]);

        Color moveColor = (pieceChar == 'O' || pieceChar == 'Q') ? Color.WHITE : Color.BLACK;
        PieceType type;
        if (pieceChar == 'Q') {
          type = PieceType.WHITE_QUEEN;
        } else if (pieceChar == 'q') {
          type = PieceType.BLACK_QUEEN;
        } else {
          type = (moveColor == Color.WHITE) ? PieceType.WHITE_PAWN : PieceType.BLACK_PAWN;
        }

        Move mainMove = new Move(fromIdx, toIdx, moveColor, type);
        List<Move> turnMoves = new java.util.ArrayList<>();
        turnMoves.add(mainMove);

        if (capturePart != null && !capturePart.isEmpty()) {
          String[] captures = capturePart.split(",");

          for (String capRaw : captures) {
            capRaw = capRaw.trim();

            if (!capRaw.isEmpty()) {
              String[] capParts = capRaw.split("\\s+");

              if (capParts.length >= 2) {
                char capPieceChar = capParts[0].charAt(0);
                int capIdx = CoordinateMapper.fromCoordinateString(capParts[1]);

                Color capColor =
                    (capPieceChar == 'O' || capPieceChar == 'Q') ? Color.WHITE : Color.BLACK;

                PieceType capType;
                if (capPieceChar == 'Q') {
                  capType = PieceType.WHITE_QUEEN;
                } else if (capPieceChar == 'q') {
                  capType = PieceType.BLACK_QUEEN;
                } else {
                  capType =
                      (moveColor == Color.WHITE) ? PieceType.WHITE_PAWN : PieceType.BLACK_PAWN;
                }

                Move capMove = new Move(capIdx, -1, capColor, capType);
                turnMoves.add(capMove);
              }
            }
          }
        }

        GameLogger.debug("History: Parsing move " + cleanMove);
        this.undoStack.push(new HistoryInformations(turnMoves, type, moveColor));
      }
    }
    GameLogger.info("History: Successfully loaded " + undoStack.size() + " turns from text.");
  }

  public HistoryInformations getHeadUndo() {
    return undoStack.peek();
  }

  public HistoryInformations getHeadRedo() {
    return redoStack.peek();
  }

  /**
   * Records a new turn in the history.
   *
   * @param informations The {@link HistoryInformations} containing the move sequence to record.
   */
  public void add(HistoryInformations informations) {
    undoStack.push(informations);
    if (!redoStack.isEmpty()) {
      GameLogger.debug("History: Clearing redo stack (new move played, branching timeline).");
      redoStack.clear();
    }
  }

  /**
   * Reverts the last recorded turn.
   */
  public HistoryInformations undo() {
    if (undoStack.isEmpty()) {
      GameLogger.debug("History: Undo requested but stack is empty.");
      return null;
    }
    HistoryInformations informations = undoStack.pop();
    redoStack.push(informations);
    GameLogger.info("History: Undone turn (" + informations.getColor() + "). UndoStack size: " + undoStack.size());
    return informations;
  }

  /**
   * Re-applies the most recently undone turn.
   */
  public HistoryInformations redo() {
    if (redoStack.isEmpty()) {
      GameLogger.debug("History: Redo requested but stack is empty.");
      return null;
    }
    HistoryInformations informations = redoStack.pop();
    undoStack.push(informations);
    GameLogger.info("History: Redone turn (" + informations.getColor() + "). UndoStack size: " + undoStack.size());
    return informations;
  }

  public boolean isEmptyUndo() {
    return undoStack.isEmpty();
  }

  public boolean isEmptyRedo() {
    return redoStack.isEmpty();
  }

  public List<HistoryInformations> toList() {
    return new ArrayList<>(undoStack);
  }

  /**
   * Converts the list of played turns into ABA-pro text format for saving.
   */
  public List<String> toTextList() {
    GameLogger.debug("History: Generating text list of moves for saving...");
    List<String> textMoves = new java.util.ArrayList<>();

    for (HistoryInformations info : this.undoStack) {
      if (info.getMoves().isEmpty()) {
        continue;
      }

      Move primaryMove = info.getMoves().get(0);

      String start = CoordinateMapper.toCoordinate(primaryMove.getFrom());
      String end = CoordinateMapper.toCoordinate(primaryMove.getDestination());

      char pieceChar;
      if (info.getColor() == Color.WHITE) {
        pieceChar = (primaryMove.getPieceType() == PieceType.WHITE_QUEEN) ? 'Q' : 'O';
      } else {
        pieceChar = (primaryMove.getPieceType() == PieceType.BLACK_QUEEN) ? 'q' : 'X';
      }

      StringBuilder moveBuilder = new StringBuilder();
      moveBuilder.append(pieceChar).append(" ").append(start).append(" ").append(end);

      if (info.getMoves().size() > 1) {
        moveBuilder.append(" (");

        for (int i = 1; i < info.getMoves().size(); i++) {
          Move capMove = info.getMoves().get(i);

          String capCoord = CoordinateMapper.toCoordinate(capMove.getFrom());

          char capChar;
          if (capMove.getColor() == Color.WHITE) {
            capChar = (capMove.getPieceType() == PieceType.WHITE_QUEEN) ? 'Q' : 'O';
          } else {
            capChar = (capMove.getPieceType() == PieceType.BLACK_QUEEN) ? 'q' : 'X';
          }

          moveBuilder.append(capChar).append(" ").append(capCoord);

          if (i < info.getMoves().size() - 1) {
            moveBuilder.append(", ");
          }
        }

        moveBuilder.append(")");
      }

      textMoves.add(moveBuilder.toString());
    }

    return textMoves;
  }
}