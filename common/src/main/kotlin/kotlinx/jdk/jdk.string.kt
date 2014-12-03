package kotlinx.jdk.string


public fun String.fromEnd(howManyFromEnd: Int): String = this.substring(this.length-howManyFromEnd)
public fun String.fromStart(howManyFromStart: Int): String = this.substring(0, howManyFromStart)
public fun String.exceptEnding(allButThisMany: Int): String = this.substring(0, this.length-allButThisMany)
public fun String.exceptLast(): String = this.substring(0, this.length()-1)
public fun String.exceptStarting(allAfterThisMany: Int): String = this.substring(allAfterThisMany)
public fun String.exceptFirst(): String = this.substring(1)

public val String.length: Int
  get() { return this.length() }

