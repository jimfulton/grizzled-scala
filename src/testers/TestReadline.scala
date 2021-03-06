// Compile this one before running it.

import grizzled.readline._
import grizzled.readline.Readline.ReadlineType._
import grizzled.readline.Readline.ReadlineType

import java.io.File

/**
 * Simple tester.
 */
object TestReadline
{
    val types = Map("editline" -> ReadlineType.EditLine,
                    "readline" -> ReadlineType.GNUReadline,
                    "getline"  -> ReadlineType.GetLine,
                    "jline"    -> ReadlineType.JLine,
                    "simple"   -> ReadlineType.Simple)

    /**
     * Default list of readline libraries to try, in order.
     */
    val DefaultReadlineLibraryList = List(ReadlineType.EditLine,
                                          ReadlineType.GNUReadline,
                                          ReadlineType.GetLine,
                                          ReadlineType.JLine,
                                          ReadlineType.Simple)

    def main(args: Array[String]) =
    {
        val readlineLibs =
            if (args.length == 0)
                DefaultReadlineLibraryList
            else
                args.toList.map(types(_))

        val r = loadReadline(readlineLibs)
        println("Using: " + r)

        val HistoryPath = "/tmp/readline.hist"
        val historyFile = new File(HistoryPath)
        if (historyFile.exists)
        {
            println("Loading history file \"" + HistoryPath + "\"")
            r.history.load(HistoryPath)
        }

        object completer extends Completer
        {
            val completions = List("linux", "lisa", "mac", "freebsd", "freedos")
            val pathCompleter = new PathnameCompleter

            def complete(token: String,
                         allTokens: List[CompletionToken],
                         line: String): List[String] =
            {
                println("token=\"" + token + "\", line=\"" + line + "\"")
                println("tokens=" + allTokens)

                allTokens match
                {
                    case Cursor :: rest =>
                        completions
                    case LineToken(s) :: Cursor :: rest =>
                        completions.filter(_.startsWith(s))
                    case LineToken(s) :: Delim :: Cursor :: rest =>
                        pathCompleter.complete("", Cursor :: rest, line)
                    case LineToken(s1) :: Delim :: LineToken(s2) :: Cursor :: rest =>
                        pathCompleter.complete(s2, Cursor :: rest, line)
                    case _ =>
                        Nil
                }
            }
        }

        r.completer = completer
        var line = r.readline("? ")
        while (line != None)
        {
            val s = line.get
            if (s != "")
            {
                if ((s == "history") || (s == "h"))
                {
                    for ((s, i) <- r.history.get.zipWithIndex)
                        println(i + ": " + s)
                }

                else
                {
                    r.history += s
                    println(s)
                }
            }

            line = r.readline("? ")
        }

        println("Saving history to file \"" + HistoryPath + "\"")
        r.history.save(HistoryPath)
    }

    private def loadReadline(libs: List[ReadlineType]) =
    {
        Readline.findReadline(libs, "TestReadline", false) match
        {
            case None    => throw new Exception("Can't find readline library.")
            case Some(r) => r
        }
    }
}
