package com.example.myalarmclock

import com.google.gson.annotations.SerializedName

data class Query(var pages : String)

data class Pages(@SerializedName("PageNumber") var pagenumber: Map<*, PageNumber> )

data class PageNumber(var revisions : String)

data class Revisions(@SerializedName("Num") var num: Map<*, Num>)

data class Num(var asterisk : String)

