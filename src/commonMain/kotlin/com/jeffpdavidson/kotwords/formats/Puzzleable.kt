package com.jeffpdavidson.kotwords.formats

import com.jeffpdavidson.kotwords.model.Puzzle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Exception thrown when puzzles are in an invalid format. */
open class InvalidFormatException(message: String) : Exception(message)

/** Base class for data that can be parsed as a [Puzzle]. */
abstract class Puzzleable {

    private val cachedPuzzleMutex = Mutex()
    private var cachedPuzzle: Puzzle? = null

    /**
     * Parse and return the data as a [Puzzle].
     *
     * The result will be cached across multiple calls to [asPuzzle].
     */
    suspend fun asPuzzle(): Puzzle {
        cachedPuzzleMutex.withLock {
            if (cachedPuzzle == null) {
                cachedPuzzle = createPuzzle()
            }
            return cachedPuzzle!!
        }
    }

    /** Parse and return the data as a [Puzzle]. */
    protected abstract suspend fun createPuzzle(): Puzzle

    /**
     * Return the data as Across Lite binary.
     *
     * An implementation based on [asPuzzle] is provided by default, but subclasses may override if a direct conversion
     * is available.
     *
     * @param solved If true, the grid will be filled in with the correct solution.
     * @param writeUtf8 If true, clues and metadata will be written directly as UTF-8 characters, if needed. This
     *                  uses the 2.0 version of the Across Lite format, which may not be supported by all
     *                  applications. If false, clues and metadata will be written as ISO-8859-1 characters, and
     *                  unsupported characters will be substituted or dropped.
     */
    open suspend fun asAcrossLiteBinary(solved: Boolean = false, writeUtf8: Boolean = true): ByteArray {
        return AcrossLite.asAcrossLiteBinary(asPuzzle(), solved = solved, writeUtf8 = writeUtf8)
    }

    
}

/**
 * Base class for data that can be parsed as a [Puzzleable].
 *
 * The main use case is for defining [Puzzleable] classes which compose one or more other [Puzzleable] containers.
 */
abstract class DelegatingPuzzleable : Puzzleable() {
    abstract suspend fun getPuzzleable(): Puzzleable

    override suspend fun createPuzzle(): Puzzle = getPuzzleable().asPuzzle()
    override suspend fun asAcrossLiteBinary(solved: Boolean, writeUtf8: Boolean): ByteArray =
        getPuzzleable().asAcrossLiteBinary(solved, writeUtf8)

    
}