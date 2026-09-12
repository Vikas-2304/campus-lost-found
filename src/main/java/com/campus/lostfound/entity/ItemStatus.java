package com.campus.lostfound.entity;

public enum ItemStatus {
    OPEN,      // active report
    MATCHED,   // a possible match was found
    CLAIMED,   // a claim was approved
    CLOSED     // owner closed / item returned
}
