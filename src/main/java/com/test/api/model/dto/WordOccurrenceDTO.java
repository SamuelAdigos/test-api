package com.test.api.model.dto;

import com.test.api.model.data.WordOccurrence;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class WordOccurrenceDTO {
    private String word;
    private Integer count;

    public static WordOccurrenceDTO toWordOccuranceDTO(WordOccurrence wordOccurrence) {
        if (wordOccurrence == null) {
            return null;
        }
        WordOccurrenceDTO wordOccurrenceDTO = new WordOccurrenceDTO();
        wordOccurrenceDTO.setWord(wordOccurrence.getWord());
        wordOccurrenceDTO.setCount(wordOccurrence.getCount());

        return wordOccurrenceDTO;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
