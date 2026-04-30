package org.unamur.service.impl;
import org.springframework.stereotype.Service;
import org.unamur.enums.FileStatus;
import org.unamur.persistence.FileDiffMetadata;
import org.unamur.persistence.LineDiffMetadata;
import org.unamur.persistence.PullRequestMetadata;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiffVisualizerService {

    static class FileDiff {
        String filename;
        FileStatus status = FileStatus.MODIFIED; // Default to modified
        List<Chunk> chunks = new ArrayList<>();
    }

    static class Chunk {
        int addedLines = 0;
        int deletedLines = 0;
    }

    /**
     * Parses the raw diff string and constructs the full JPA Metadata hierarchy.
     */
    public PullRequestMetadata buildEntitiesFromDiff(String owner, String repository, int prNumber, String diffContent) {

        // Initialize the Root Metadata Entity
        PullRequestMetadata prMetadata = new PullRequestMetadata();
        prMetadata.setOwner(owner);
        prMetadata.setRepository(repository);
        prMetadata.setPrNumber(prNumber);

        String[] lines = diffContent.split("\n");

        FileDiffMetadata currentFile = null;
        LineDiffMetadata currentLineDiff = null;
        StringBuilder currentLineDiffCode = new StringBuilder();

        int chunkIndex = 0;

        for (String line : lines) {

            // --- DETECT NEW FILE ---
            if (line.startsWith("diff --git")) {

                // Save the previous line diff block if we were working on one
                if (currentLineDiff != null) {
                    currentLineDiff.setRawCodeLines(currentLineDiffCode.toString());
                    currentFile.addChunk(currentLineDiff);
                }
                // Save the previous file if we were working on one
                if (currentFile != null) {
                    prMetadata.addFile(currentFile);
                }

                // Start a new File Metadata
                currentFile = new FileDiffMetadata();
                currentFile.setStatus(FileStatus.MODIFIED); // Default assumption

                // Extract filename. Git diffs format paths like: a/src/App.java b/src/App.java
                String[] parts = line.split(" ");
                String filename = parts[parts.length - 1].replaceFirst("^b/", "");
                currentFile.setFilename(filename);

                // Reset trackers for the new file
                currentLineDiff = null;
                chunkIndex = 0;
            }

            // --- DETECT FILE STATUS ---
            else if (currentFile != null) {
                if (line.startsWith("new file mode")) {
                    currentFile.setStatus(FileStatus.NEW);
                } else if (line.startsWith("deleted file mode")) {
                    currentFile.setStatus(FileStatus.DELETED);
                }

                // --- DETECT NEW LINE DIFF BLOCK (Starts with @@) ---
                else if (line.startsWith("@@")) {
                    // Close out the previous block if it exists
                    if (currentLineDiff != null) {
                        currentLineDiff.setRawCodeLines(currentLineDiffCode.toString());
                        currentFile.addChunk(currentLineDiff);
                    }

                    // Initialize new LineDiffMetadata
                    currentLineDiff = new LineDiffMetadata();
                    currentLineDiff.setChunkIndex(chunkIndex++);
                    currentLineDiff.setAddedLinesCount(0);
                    currentLineDiff.setDeletedLinesCount(0);

                    // Reset the string builder and append the header (e.g., @@ -1,3 +1,4 @@)
                    currentLineDiffCode = new StringBuilder();
                    currentLineDiffCode.append(line).append("\n");
                }

                // --- PROCESS CODE LINES INSIDE A DIFF BLOCK ---
                else if (currentLineDiff != null) {
                    currentLineDiffCode.append(line).append("\n");

                    // Increment our counters for the SVG logic
                    if (line.startsWith("+") && !line.startsWith("+++")) {
                        currentLineDiff.setAddedLinesCount(currentLineDiff.getAddedLinesCount() + 1);
                    } else if (line.startsWith("-") && !line.startsWith("---")) {
                        currentLineDiff.setDeletedLinesCount(currentLineDiff.getDeletedLinesCount() + 1);
                    }
                }
            }
        }

        // Attach the very last diff block and file when the loop ends
        if (currentLineDiff != null) {
            currentLineDiff.setRawCodeLines(currentLineDiffCode.toString());
            currentFile.addChunk(currentLineDiff);
        }
        if (currentFile != null) {
            prMetadata.addFile(currentFile);
        }

        return prMetadata;
    }

    /**
     * Parses a diff content into a list of FileDiff entities.
     *
     * @param diffContent The diff content to parse.
     * @return A list of FileDiff entities representing the parsed diff.
     */
    public List<FileDiff> parseDiff(String diffContent) {
        List<FileDiff> files = new ArrayList<>();
        FileDiff currentFile = null;
        Chunk currentChunk = null;

        String[] lines = diffContent.split("\n");

        for (String line : lines) {
            // New file block starts
            if (line.startsWith("diff --git")) {
                if (currentFile != null) files.add(currentFile);
                currentFile = new FileDiff();
                // Extract filename (simplistic extraction from "b/..." path)
                String[] parts = line.split(" ");
                currentFile.filename = parts[parts.length - 1].replace("b/", "");
                currentChunk = null;
            }
            else if (currentFile != null) {
                if (line.startsWith("new file mode")) {
                    currentFile.status = FileStatus.NEW;
                } else if (line.startsWith("deleted file mode")) {
                    currentFile.status = FileStatus.DELETED;
                } else if (line.startsWith("@@")) {
                    // New chunk of changes
                    currentChunk = new Chunk();
                    currentFile.chunks.add(currentChunk);
                } else if (currentChunk != null) {
                    // Count lines, ignoring the +++ and --- headers
                    if (line.startsWith("+") && !line.startsWith("+++")) {
                        currentChunk.addedLines++;
                    } else if (line.startsWith("-") && !line.startsWith("---")) {
                        currentChunk.deletedLines++;
                    }
                }
            }
        }
        if (currentFile != null) files.add(currentFile);
        return files;
    }

    /**
     * Generates an SVG visualization from a diff content.
     *
     * @param diffContent The diff content to visualize.
     * @return The SVG visualization as a string.
     */
    public String generateSvgFromDiff(String diffContent) {
        List<FileDiff> files = parseDiff(diffContent);
        StringBuilder svg = new StringBuilder();

        // SVG wrapper setup
        int padding = 40;
        int currentX = padding;
        int currentY = padding;
        int rowHeight = 0;
        int maxWidth = 800; // Will wrap to next line if it exceeds this

        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100%\" height=\"100%\" >\n");
        svg.append("<style>.text { font-family: sans-serif; font-size: 12px; fill: #333; }</style>\n");

        for (FileDiff file : files) {
            int elementWidth = 0;
            int elementHeight = 0;
            StringBuilder elementSvg = new StringBuilder();

            if (file.status == FileStatus.NEW) {
                elementWidth = 40; elementHeight = 40;
                elementSvg.append(String.format("<circle cx=\"%d\" cy=\"%d\" r=\"15\" fill=\"#22c55e\" />", currentX + 20, currentY + 20));

            } else if (file.status == FileStatus.DELETED) {
                elementWidth = 40; elementHeight = 40;
                elementSvg.append(String.format("<circle cx=\"%d\" cy=\"%d\" r=\"15\" fill=\"#ef4444\" />", currentX + 20, currentY + 20));

            } else if (file.status == FileStatus.MODIFIED) {
                int innerX = currentX + 10;
                int innerY = currentY + 10;
                int maxInnerHeight = 0;
                int chunkIndex = 0; // Keep track of the chunk index

                for (Chunk chunk : file.chunks) {
                    // Draw Added square with data attributes
                    if (chunk.addedLines > 0) {
                        int size = getSquareSize(chunk.addedLines);
                        elementSvg.append(String.format(
                                "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"#22c55e\" " +
                                        "class=\"interactive-chunk\" data-type=\"chunk\" data-filename=\"%s\" data-chunk-index=\"%d\" style=\"cursor:pointer;\" />\n",
                                innerX, innerY, size, size, file.filename, chunkIndex));
                        innerX += size + 5;
                        maxInnerHeight = Math.max(maxInnerHeight, size);
                    }
                    // Draw Deleted square with data attributes
                    if (chunk.deletedLines > 0) {
                        int size = getSquareSize(chunk.deletedLines);
                        elementSvg.append(String.format(
                                "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"#ef4444\" " +
                                        "class=\"interactive-chunk\" data-type=\"chunk\" data-filename=\"%s\" data-chunk-index=\"%d\" style=\"cursor:pointer;\" />\n",
                                innerX, innerY, size, size, file.filename, chunkIndex));
                        innerX += size + 5;
                        maxInnerHeight = Math.max(maxInnerHeight, size);
                    }
                    chunkIndex++;
                }

                elementWidth = (innerX - currentX) + 10;
                elementHeight = maxInnerHeight + 20;

                // Draw the wrapping transparent square with data attributes
                elementSvg.insert(0, String.format(
                        "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"transparent\" stroke=\"#334155\" stroke-width=\"3\" rx=\"4\" " +
                                "class=\"interactive-file\" data-type=\"file\" data-filename=\"%s\" style=\"cursor:pointer;\" />\n",
                        currentX, currentY, elementWidth, elementHeight, file.filename));
            }

            // Add filename label
            elementSvg.append(String.format("\n<text x=\"%d\" y=\"%d\" class=\"text\">%s</text>\n", currentX, currentY + elementHeight + 15, file.filename));
            elementHeight += 20; // add space for text

            // --- Grid Wrapping Logic ---
            if (currentX + elementWidth > maxWidth) {
                currentX = padding;
                currentY += rowHeight + padding;
                rowHeight = 0;
            }

            svg.append(elementSvg.toString());

            currentX += elementWidth + 30; // 30px gap between files
            rowHeight = Math.max(rowHeight, elementHeight);
        }

        // Adjust final SVG ViewBox size dynamically based on layout
        int totalHeight = currentY + rowHeight + padding;
        svg.insert(svg.indexOf(">") + 1, String.format(" viewBox=\"0 0 900 %d\"", totalHeight));
        svg.append("</svg>");

        return svg.toString();
    }

    /**
     * Determines the size of a square based on the number of lines.
     *
     * @param linesCount The number of lines in the square.
     * @return The size of the square in pixels.
     */
    private int getSquareSize(int linesCount) {
        if (linesCount == 0) return 0;
        if (linesCount <= 5) return 10;      // Small square
        if (linesCount <= 20) return 20;     // Medium square
        return 30;                           // Big square
    }
}