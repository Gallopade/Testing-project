-- ============================================================================
-- SQL Script: Delete Student Class Assignment Answer Details
-- ============================================================================
-- Description: 
--   This script deletes all student answer details for a specific class assignment.
--   It removes records from studentClassAssignmentAnswerdetails table by
--   joining with studentClassAssignmentAnswer and studentClassAssignment tables.
--
-- Parameters:
--   ClassAssignmentId: '39c0d889-5000-4355-b0c4-7e4df6a9dda3'
--
-- Tables Affected:
--   - studentClassAssignmentAnswerdetails (scaad)
--   - studentClassAssignmentAnswer (scaa) - referenced in JOIN
--   - studentClassAssignment (sca) - referenced in JOIN
--
-- Usage:
--   Update the ClassAssignmentId in the WHERE clause before executing.
-- ============================================================================

DELETE scaad
FROM studentClassAssignmentAnswerdetails scaad
JOIN studentClassAssignmentAnswer scaa
  ON scaad.studentClassAssignmentAnswerId = scaa.studentClassAssignmentAnswerId
JOIN studentClassAssignment sca
  ON scaa.studentClassAssignmentId = sca.studentClassAssignmentId
WHERE sca.ClassAssignmentId = '39c0d889-5000-4355-b0c4-7e4df6a9dda3';

-- ============================================================================
-- Optional: Verify deletion (uncomment to check affected rows before deletion)
-- ============================================================================
-- SELECT COUNT(*) as records_to_delete
-- FROM studentClassAssignmentAnswerdetails scaad
-- JOIN studentClassAssignmentAnswer scaa
--   ON scaad.studentClassAssignmentAnswerId = scaa.studentClassAssignmentAnswerId
-- JOIN studentClassAssignment sca
--   ON scaa.studentClassAssignmentId = sca.studentClassAssignmentId
-- WHERE sca.ClassAssignmentId = '39c0d889-5000-4355-b0c4-7e4df6a9dda3';
-- ============================================================================

