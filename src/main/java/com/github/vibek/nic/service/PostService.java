package com.github.vibek.nic.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.vibek.nic.entity.Post;
import com.github.vibek.nic.repository.PostRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPostById(UUID id) {
        return postRepository.findById(id);
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public Post updatePost(UUID id, Post postDetails) {
        return postRepository.findById(id).map(post -> {
            post.setPostName(postDetails.getPostName());
            post.setRank(postDetails.getRank());
            post.setDepartment(postDetails.getDepartment());
            return postRepository.save(post);
        }).orElse(null);
    }

    public boolean deletePost(UUID id) {
        return postRepository.findById(id).map(post -> {
            postRepository.delete(post);
            return true;
        }).orElse(false);
    }
}

