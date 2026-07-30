package com.revature.DAOs;

import com.revature.exceptions.ResourceNotFoundException;
import com.revature.models.Approval;
import com.revature.utils.ConnectionUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApprovalDAOIT {

        private static final String H2_URL = "jdbc:h2:mem:approvaldb;DB_CLOSE_DELAY=-1";

        private ApprovalDAO approvalDAO;
        private MockedStatic<ConnectionUtil> connectionUtilMock;

        @BeforeAll
        static void createSchema() throws SQLException {
            try (Connection conn = DriverManager.getConnection(H2_URL);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE approvals (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        expense_id INT NOT NULL,
                        status VARCHAR(20),
                        reviewer INT,
                        comment VARCHAR(255),
                        review_date VARCHAR(20)
                    )
                """);
            }
        }

        @AfterAll
        static void dropDatabase() throws SQLException {
            try (Connection conn = DriverManager.getConnection(H2_URL);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE approvals");
            }
        }

        @BeforeEach
        void setUp() throws SQLException {
            approvalDAO = new ApprovalDAO();

            connectionUtilMock = mockStatic(ConnectionUtil.class);
            
            connectionUtilMock.when(ConnectionUtil::getConnection)
                    .thenAnswer(invocation -> DriverManager.getConnection(H2_URL));

            // start each test with a clean state
            try (Connection conn = DriverManager.getConnection(H2_URL);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM approvals");
            }
        }

        @AfterEach
        void tearDown() {
            connectionUtilMock.close();
        }

        //getApprovalByExpenseId
        @Test
        void getApprovalByExpenseId_happyPath_returnsRealRow() throws SQLException {
            insertApproval(42, "pending", 7, "Awaiting review", "2026-07-20");

            Approval result = approvalDAO.getApprovalByExpenseId(42);

            assertNotNull(result);
            assertEquals(42, result.getExpenseId());
            assertEquals("pending", result.getStatus());
            assertEquals(7, result.getReviewer());
            assertEquals("Awaiting review", result.getComment());
            assertEquals("2026-07-20", result.getReviewDate());
        }

        @Test
        void getApprovalByExpenseId_sadPath_noMatchingRow_returnsNull() {
            // table is empty per setUp(); no insert for this expense id
            Approval result = approvalDAO.getApprovalByExpenseId(999);

            assertNull(result);
        }

        //updateApproval

        @Test
        void updateApproval_happyPath_actuallyPersistsChange() throws SQLException {
            insertApproval(42, "pending", 0, "", "2026-07-01");

            boolean result = approvalDAO.updateApproval(42, "approved", 7, "Looks good");

            assertTrue(result);

            // reread DAO to confirm
            Approval updated = approvalDAO.getApprovalByExpenseId(42);
            assertNotNull(updated);
            assertEquals("approved", updated.getStatus());
            assertEquals(7, updated.getReviewer());
            assertEquals("Looks good", updated.getComment());
        }

        @Test
        void updateApproval_sadPath_noMatchingRow_throwsResourceNotFoundException() {
            //table is empty
            //expense_id 999 matches nothing
            assertThrows(ResourceNotFoundException.class, () ->
                    approvalDAO.updateApproval(999, "approved", 7, "comment"));
        }

        //test helper!
        private void insertApproval(int expenseId, String status, int reviewer, String comment, String reviewDate) throws SQLException {
            String sql = "INSERT INTO approvals (expense_id, status, reviewer, comment, review_date) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(H2_URL);
                 var ps = conn.prepareStatement(sql)) {
                ps.setInt(1, expenseId);
                ps.setString(2, status);
                ps.setInt(3, reviewer);
                ps.setString(4, comment);
                ps.setString(5, reviewDate);
                ps.executeUpdate();
            }
        }
    }



