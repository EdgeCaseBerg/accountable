CREATE TABLE expenseGroups (
	name VARCHAR(64) NOT NULL UNIQUE KEY,
	groupId CHAR(36) NOT NULL PRIMARY KEY
) ENGINE InnoDB DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE='utf8mb4_unicode_ci';

CREATE TABLE expenseGroupToExpense (
	groupId CHAR(36) NOT NULL, 
	expenseId CHAR(36) NOT NULL UNIQUE KEY,
	FOREIGN KEY expenseGroupToExpense_to_groupId (groupId) REFERENCES expenseGroups(groupId), 
	FOREIGN KEY expenseGroupToExpense_to_expenseId (expenseId) REFERENCES expenses(expenseId)
) ENGINE InnoDB DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE='utf8mb4_unicode_ci';