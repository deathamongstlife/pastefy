package cc.allyapps.pastely.websocket;

import cc.allyapps.pastely.websocket.model.TextOperation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Operational Transform algorithm.
 */
public class OperationalTransformTest {

    @Test
    public void testInsertVsInsert_DifferentPositions() {
        TextOperation opA = new TextOperation("insert", 5, "A", 0, "user1");
        TextOperation opB = new TextOperation("insert", 10, "B", 0, "user2");

        TextOperation transformed = OperationalTransform.transform(opA, opB);

        assertEquals(5, transformed.getPosition());
        assertEquals("A", transformed.getText());
    }

    @Test
    public void testInsertVsInsert_SamePosition() {
        TextOperation opA = new TextOperation("insert", 5, "A", 0, "user1");
        TextOperation opB = new TextOperation("insert", 5, "B", 0, "user2");

        TextOperation transformed = OperationalTransform.transform(opA, opB);

        // Position adjusted based on user ID comparison
        assertTrue(transformed.getPosition() == 5 || transformed.getPosition() == 6);
    }

    @Test
    public void testInsertVsDelete() {
        TextOperation opA = new TextOperation("insert", 10, "A", 0, "user1");
        TextOperation opB = new TextOperation("delete", 5, "12345", 0, "user2");

        TextOperation transformed = OperationalTransform.transform(opA, opB);

        assertEquals(5, transformed.getPosition());
        assertEquals("A", transformed.getText());
    }

    @Test
    public void testDeleteVsInsert() {
        TextOperation opA = new TextOperation("delete", 10, "ABC", 0, "user1");
        TextOperation opB = new TextOperation("insert", 5, "X", 0, "user2");

        TextOperation transformed = OperationalTransform.transform(opA, opB);

        assertEquals(11, transformed.getPosition());
        assertEquals("ABC", transformed.getText());
    }

    @Test
    public void testApplyInsertOperation() {
        String document = "Hello World";
        TextOperation operation = new TextOperation("insert", 6, "Beautiful ", 0, "user1");

        String result = OperationalTransform.applyOperation(document, operation);

        assertEquals("Hello Beautiful World", result);
    }

    @Test
    public void testApplyDeleteOperation() {
        String document = "Hello World";
        TextOperation operation = new TextOperation("delete", 6, "World", 0, "user1");

        String result = OperationalTransform.applyOperation(document, operation);

        assertEquals("Hello ", result);
    }

    @Test
    public void testApplyReplaceOperation() {
        String document = "Hello World";
        TextOperation operation = new TextOperation("replace", 6, "Earth", 0, "user1");

        String result = OperationalTransform.applyOperation(document, operation);

        assertEquals("Hello Earth", result);
    }

    @Test
    public void testIsValidOperation() {
        TextOperation operation = new TextOperation("insert", 5, "A", 5, "user1");

        assertTrue(OperationalTransform.isValidOperation(operation, 5));
        assertFalse(OperationalTransform.isValidOperation(operation, 4));
        assertFalse(OperationalTransform.isValidOperation(operation, 6));
    }

    @Test
    public void testApplyOperationOutOfBounds() {
        String document = "Hello";
        TextOperation operation = new TextOperation("insert", 100, "X", 0, "user1");

        String result = OperationalTransform.applyOperation(document, operation);

        assertEquals("HelloX", result);
    }

    @Test
    public void testApplyDeleteOperationOutOfBounds() {
        String document = "Hello";
        TextOperation operation = new TextOperation("delete", 3, "abcdefgh", 0, "user1");

        String result = OperationalTransform.applyOperation(document, operation);

        assertEquals("Hel", result);
    }

    @Test
    public void testTransformWithNullOperations() {
        TextOperation opA = new TextOperation("insert", 5, "A", 0, "user1");

        TextOperation transformed = OperationalTransform.transform(opA, null);

        assertEquals(opA, transformed);
    }

    @Test
    public void testConcurrentEditsScenario() {
        String initial = "Hello World";

        // User 1: Insert "Beautiful " at position 6
        TextOperation op1 = new TextOperation("insert", 6, "Beautiful ", 0, "user1");
        String afterOp1 = OperationalTransform.applyOperation(initial, op1);
        assertEquals("Hello Beautiful World", afterOp1);

        // User 2: Delete "World" at position 6 (concurrent with op1)
        TextOperation op2 = new TextOperation("delete", 6, "World", 0, "user2");

        // Transform op2 against op1
        TextOperation transformedOp2 = OperationalTransform.transform(op2, op1);

        // Apply transformed op2
        String result = OperationalTransform.applyOperation(afterOp1, transformedOp2);

        assertEquals("Hello Beautiful ", result);
    }
}
