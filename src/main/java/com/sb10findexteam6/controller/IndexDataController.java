package com.sb10findexteam6.controller;

import com.sb10findexteam6.dto.indexdata.IndexDataCreateRequest;
import com.sb10findexteam6.dto.indexdata.IndexDataDto;
import com.sb10findexteam6.dto.indexdata.IndexDataUpdateRequest;
import com.sb10findexteam6.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {
    private final IndexDataService indexDataService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IndexDataDto create(@RequestBody IndexDataCreateRequest request) {
        return indexDataService.create(request);
    }

    @PatchMapping("{id}")
    public IndexDataDto update(
            @PathVariable Long id,
            @RequestBody IndexDataUpdateRequest request
    ) {
        return indexDataService.update(id, request);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        indexDataService.delete(id);
    }

    @GetMapping("/{id}")
    public IndexDataDto getById(@PathVariable Long id) {
        return indexDataService.getById(id);
    }

}
