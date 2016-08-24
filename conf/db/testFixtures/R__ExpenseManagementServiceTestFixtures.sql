INSERT INTO expenseGroups (name, groupId) VALUES ("Fixture Group","b95bd45d-f654-42fb-8cea-7c7e8db230e9");
INSERT INTO expenses (amountInCents, name, dateOccured, expenseId) VALUES 
(200, "My Fixture Expense", CURDATE(), "07f256f0-f830-495d-b6cb-fe39bb5f2f36");
INSERT INTO expenseGroupToExpense (groupId, expenseId) VALUES ("b95bd45d-f654-42fb-8cea-7c7e8db230e9", "07f256f0-f830-495d-b6cb-fe39bb5f2f36");