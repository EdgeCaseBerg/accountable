CREATE TABLE expenseGroups (
	name VARCHAR(64) NOT NULL UNIQUE KEY,
	groupId CHAR(36) NOT NULL PRIMARY KEY
) ENGINE InnoDB DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE='utf8mb4_unicode_ci';

CREATE TABLE expenseGroupToExpense (
	groupId CHAR(36) FOREIGN KEY expenseGroups(groupId),
	expenseId CHAR(36) FOREIGN KEY expense(expenseId)
) ENGINE InnoDB DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE='utf8mb4_unicode_ci';