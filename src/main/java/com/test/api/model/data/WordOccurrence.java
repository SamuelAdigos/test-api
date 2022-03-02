package com.test.api.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "word_occurrences")
@NoArgsConstructor
@AllArgsConstructor
public class WordOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "word")
    private String word;

    @Column(name = "count")
    private Integer count = 0;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="file_metrics_id", nullable=false)
    @JsonIgnore
    private FileMetrics fileMetrics;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public FileMetrics getFileMetrics() {
        return fileMetrics;
    }

    public void setFileMetrics(FileMetrics fileMetrics) {
        this.fileMetrics = fileMetrics;
    }
}
