package com.muje.capstone.service.Community;

import com.muje.capstone.domain.Community.Comment;
import com.muje.capstone.domain.Community.Discussion;
import com.muje.capstone.domain.Community.Post;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.Community.Comment.AddCommentRequest;
import com.muje.capstone.dto.Community.Comment.CommentResponse;
import com.muje.capstone.dto.User.Point.PointRequest;
import com.muje.capstone.repository.Community.CommentRepository;
import com.muje.capstone.repository.Community.PostRepository;
import com.muje.capstone.repository.User.UserRepository;
import com.muje.capstone.service.NotificationService;
import com.muje.capstone.service.User.PointService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PointService pointService; // PointService 주입

    private static final int ADOPTION_POINTS = 50; // 채택 시 지급할 포인트 (예시)

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 유지
    public List<CommentResponse> getCommentsByUser(String userEmail) {
        List<Comment> comments = commentRepository.findByUserEmail(userEmail);
        for (Comment comment : comments) {
            comment.getReplies().size();
            if (comment.getParentComment() != null) {
                comment.getParentComment().getId();
            }
        }
        return comments.stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }

    // 1) 포스트의 최상위 댓글 + 각 대댓글을 함께 가져오기
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        List<Comment> roots = commentRepository.findByPostIdAndParentCommentIsNull(postId);

        // DTO 변환 + 대댓글 매핑
        return roots.stream()
                .map(root -> {
                    CommentResponse dto = new CommentResponse(root);
                    List<Comment> replies = commentRepository.findByParentCommentId(root.getId());
                    replies.forEach(reply -> dto.getReplies().add(new CommentResponse(reply)));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 2) 댓글 작성 (parentCommentId가 있으면 대댓글)
    @Transactional
    public Comment addComment(Long postId, AddCommentRequest req, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        Comment parent = null;
        if (req.getParentCommentId() != null) {
            parent = commentRepository.findById(req.getParentCommentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
            parent.incrementCommentCount();
            commentRepository.save(parent);
        }

        post.incrementCommentCount();
        postRepository.save(post);

        Comment comment = commentRepository.save(
                Comment.builder()
                        .post(post)
                        .user(user)
                        .content(req.getContent())
                        .parentComment(parent)
                        .build()
        );

        // 대댓글이면 → 원댓글 작성자에게 알림
        if (parent != null && !parent.getUser().getId().equals(user.getId())) {
            String parentAuthorEmail = parent.getUser().getEmail();
            notificationService.createCommentNotification(
                    parentAuthorEmail,
                    user.getNickname() + "님이 회원님의 댓글에 답글을 달았습니다: " + req.getContent(),
                    postId,
                    comment.getId()
            );
        }

        // 대댓글이 아니고, 댓글 작성자가 게시글 작성자와 다르면 → 게시글 작성자에게 알림
        else if (parent == null && !post.getUser().getId().equals(user.getId())) {
            String postOwnerEmail = post.getUser().getEmail();
            notificationService.createCommentNotification(
                    postOwnerEmail,
                    user.getNickname() + "님이 회원님의 게시글에 댓글을 달았습니다: " + req.getContent(),
                    postId,
                    comment.getId()
            );
        }

        return comment;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        validateCommentOwner(comment); // 작성자 검증

        Post post = comment.getPost();

        Comment parent = comment.getParentComment();
        if (parent != null) {
            parent.decrementCommentCount();
            post.decrementCommentCount();
            commentRepository.save(parent);
            postRepository.save(post);

        } else {
            int numberOfRepliesToDelete = comment.getReplies().size();
            post.decrementCommentCount(); // 최상위 댓글 1개 감소
            for (int i = 0; i < numberOfRepliesToDelete; i++) {
                post.decrementCommentCount(); // 대댓글 수 만큼 추가 감소
            }
            postRepository.save(post);
        }
        commentRepository.delete(comment);
    }

    // 댓글을 작성한 유저인지 확인 (User 엔티티의 이메일로 비교)
    private static void validateCommentOwner(Comment comment) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (comment.getUser() == null || !comment.getUser().getEmail().equals(userName)) {
            throw new IllegalArgumentException("Not authorized to delete this comment");
        }
    }

    @Transactional
    public Comment adoptComment(Long discussionId, Long commentId, String adopterEmail) {
        Discussion discussion = (Discussion) postRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found with id: " + discussionId));

        if (!discussion.getUser().getEmail().equals(adopterEmail)) {
            throw new SecurityException("게시글 작성자만 댓글을 채택할 수 있습니다.");
        }

        if (discussion.getAdoptedCommentId() != null) {
            throw new IllegalStateException("이미 채택된 댓글이 있습니다.");
        }

        Comment commentToAdopt = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));

        if (!commentToAdopt.getPost().getId().equals(discussionId)) {
            throw new IllegalArgumentException("해당 댓글은 이 게시글의 댓글이 아닙니다.");
        }

        commentToAdopt.adopt();
        discussion.adoptComment(commentId);

        User commentAuthor = commentToAdopt.getUser();

        PointRequest pointRequest = new PointRequest();
        pointRequest.setAmount(ADOPTION_POINTS);
        pointRequest.setDescription(discussion.getTitle() + "의 채택 보상");

        pointService.accumulatePoints(commentAuthor.getEmail(), pointRequest);

        notificationService.AdoptCommentNotification(
                commentAuthor.getEmail(),
                discussion.getTitle() + " 게시글에서 댓글이 채택되어 " + ADOPTION_POINTS + "포인트를 획득했습니다!",
                discussionId,
                commentId
        );

        return commentToAdopt;
    }

    @Transactional(readOnly = true)
    public Optional<Comment> getAdoptedCommentForDiscussion(Long discussionId) {
        Discussion discussion = (Discussion) postRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found with id: " + discussionId));

        if (discussion.getAdoptedCommentId() == null) {
            return Optional.empty();
        }
        return commentRepository.findById(discussion.getAdoptedCommentId());
    }
}