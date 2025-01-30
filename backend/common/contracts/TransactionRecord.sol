// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract TransactionRecord {
    struct Transaction {
        address sender;
        string description;
        uint256 amount;
        uint256 timestamp;
    }

    Transaction[] public transactions;

    event TransactionAdded(address indexed sender, string description, uint256 amount, uint256 timestamp);

    function addTransaction(string memory description, uint256 amount) public {
        transactions.push(Transaction(msg.sender, description, amount, block.timestamp));
        emit TransactionAdded(msg.sender, description, amount, block.timestamp);
    }

    function getTransaction(uint256 index) public view returns (address, string memory, uint256, uint256) {
        Transaction memory txn = transactions[index];
        return (txn.sender, txn.description, txn.amount, txn.timestamp);
    }

    function getTransactionCount() public view returns (uint256) {
        return transactions.length;
    }
}