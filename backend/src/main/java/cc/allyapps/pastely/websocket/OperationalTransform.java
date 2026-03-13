package cc.allyapps.pastely.websocket;

import cc.allyapps.pastely.websocket.model.TextOperation;

/**
 * Operational Transform (OT) engine for conflict-free collaborative editing.
 * Implements transformation rules to ensure consistency across concurrent edits.
 */
public class OperationalTransform {

    /**
     * Transform operation A against operation B.
     * Returns transformed A that can be applied after B.
     *
     * This implements the core OT transformation algorithm:
     * - If A and B are concurrent operations, transform A against B
     * - Result can be applied after B to achieve the same final state
     */
    public static TextOperation transform(TextOperation a, TextOperation b) {
        if (a == null || b == null) {
            return a;
        }

        TextOperation transformed = new TextOperation();
        transformed.setType(a.getType());
        transformed.setText(a.getText());
        transformed.setUserId(a.getUserId());
        transformed.setVersion(a.getVersion() + 1);
        transformed.setTimestamp(a.getTimestamp());

        String typeA = a.getType();
        String typeB = b.getType();
        int posA = a.getPosition();
        int posB = b.getPosition();

        // Insert vs Insert
        if ("insert".equals(typeA) && "insert".equals(typeB)) {
            if (posA < posB || (posA == posB && a.getUserId().compareTo(b.getUserId()) < 0)) {
                transformed.setPosition(posA);
            } else {
                transformed.setPosition(posA + (b.getText() != null ? b.getText().length() : 0));
            }
        }
        // Insert vs Delete
        else if ("insert".equals(typeA) && "delete".equals(typeB)) {
            int deleteLengthB = b.getText() != null ? b.getText().length() : 0;
            if (posA <= posB) {
                transformed.setPosition(posA);
            } else if (posA > posB + deleteLengthB) {
                transformed.setPosition(posA - deleteLengthB);
            } else {
                transformed.setPosition(posB);
            }
        }
        // Delete vs Insert
        else if ("delete".equals(typeA) && "insert".equals(typeB)) {
            int insertLengthB = b.getText() != null ? b.getText().length() : 0;
            if (posA < posB) {
                transformed.setPosition(posA);
            } else {
                transformed.setPosition(posA + insertLengthB);
            }
        }
        // Delete vs Delete
        else if ("delete".equals(typeA) && "delete".equals(typeB)) {
            int deleteLengthA = a.getText() != null ? a.getText().length() : 0;
            int deleteLengthB = b.getText() != null ? b.getText().length() : 0;

            if (posA + deleteLengthA <= posB) {
                transformed.setPosition(posA);
            } else if (posA >= posB + deleteLengthB) {
                transformed.setPosition(posA - deleteLengthB);
            } else {
                // Overlapping deletes - complex case
                if (posA < posB) {
                    transformed.setPosition(posA);
                    int newLength = Math.max(0, posB - posA);
                    if (a.getText() != null && newLength < a.getText().length()) {
                        transformed.setText(a.getText().substring(0, newLength));
                    }
                } else {
                    int offset = posB + deleteLengthB - posA;
                    if (offset > 0) {
                        transformed.setPosition(posB);
                        int newLength = Math.max(0, deleteLengthA - offset);
                        if (a.getText() != null && offset < a.getText().length()) {
                            transformed.setText(a.getText().substring(offset, Math.min(a.getText().length(), offset + newLength)));
                        }
                    } else {
                        transformed.setPosition(posA - deleteLengthB);
                    }
                }
            }
        }
        // Replace operations (treat as delete + insert)
        else if ("replace".equals(typeA)) {
            transformed.setPosition(posA);
        } else {
            transformed.setPosition(posA);
        }

        return transformed;
    }

    /**
     * Apply operation to document text.
     * Returns new document state after applying the operation.
     */
    public static String applyOperation(String document, TextOperation operation) {
        if (document == null) {
            document = "";
        }
        if (operation == null) {
            return document;
        }

        int position = operation.getPosition();
        String type = operation.getType();
        String text = operation.getText() != null ? operation.getText() : "";

        try {
            switch (type) {
                case "insert":
                    if (position < 0 || position > document.length()) {
                        position = Math.max(0, Math.min(position, document.length()));
                    }
                    return document.substring(0, position) + text + document.substring(position);

                case "delete":
                    if (position < 0 || position >= document.length()) {
                        return document;
                    }
                    int deleteEnd = Math.min(document.length(), position + text.length());
                    return document.substring(0, position) + document.substring(deleteEnd);

                case "replace":
                    if (position < 0 || position >= document.length()) {
                        return document;
                    }
                    int replaceEnd = Math.min(document.length(), position + text.length());
                    return document.substring(0, position) + text + document.substring(replaceEnd);

                default:
                    return document;
            }
        } catch (Exception e) {
            System.err.println("Error applying operation: " + e.getMessage());
            return document;
        }
    }

    /**
     * Validate operation against current version.
     * Returns true if operation version matches expected version.
     */
    public static boolean isValidOperation(TextOperation operation, int currentVersion) {
        if (operation == null) {
            return false;
        }
        return operation.getVersion() == currentVersion;
    }

    /**
     * Transform operation against a list of concurrent operations.
     * Useful for handling multiple concurrent edits.
     */
    public static TextOperation transformAgainstMultiple(TextOperation operation, java.util.List<TextOperation> operations) {
        TextOperation result = operation;
        for (TextOperation op : operations) {
            result = transform(result, op);
        }
        return result;
    }
}
