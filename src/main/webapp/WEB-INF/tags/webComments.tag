<%@ tag language="java" pageEncoding="UTF-8"
	import="infinitystone.chalKag.biz.comment.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="webComments"%>

<div class="comments">
	<h5>${fn:length(commentList)} Responses</h5>
	<div style="text-align: center;">
		<!-- 로그인 했을때 -->
		<c:if test="${member != null}">
			<input style="display: inline-block;"
				class="btn btn-primary btn-rounded" id="toggleWriteBtn1"
				type="button" value="Write Responses" onclick="toggleWrite(this)">
		</c:if>
		<!-- 비 로그인 유저일때 -->
		<c:if test="${member == null}">
			<input style="display: inline-block;"
				class="btn btn-primary btn-rounded" id="toggleWriteBtn2"
				type="button" value="Write Responses" onclick="message()">
		</c:if>
	</div>
	<br>
	<!-- 댓글 입력 -->
	<div id="writeResponseForm" style="display: none; text-align: center;">
		<form id="commentForm" action="/writeComment" method="post" class="row">
				<c:if test="${not empty headHuntPostSingle}">
				    <input type="hidden" name="postId" value="${headHuntPostSingle.headHuntPostId}">
				</c:if>
				<c:if test="${not empty jobHuntPostSingle}">
				    <input type="hidden" name="postId" value="${jobHuntPostSingle.jobHuntPostId}">
				</c:if>
				<c:if test="${not empty freePostSingle}">
				    <input type="hidden" name="postId" value="${freePostSingle.freePostId}">
				</c:if>
				<c:if test="${not empty marketPostSingle}">
				    <input type="hidden" name="postId" value="${marketPostSingle.marketPostId}">
				</c:if>
					
			<div class="form-group col-md-12">
				<label for="message">Response <span class="required"></span></label>
				<textarea class="form-control" name="commentContent"
					placeholder="Write your response ..."></textarea>
			</div>
			<div class="form-group col-md-12">
				<button class="btn btn-primary">Send Response</button>
			</div>
		</form>
	</div>

	<div class="comment-list">
		<!-- 댓글이 없을 경우 출력 문구 -->
		<c:if test="${commentList == null || fn:length(commentList) <= 0}">
			<h5 style="text-align: center;">댓글이 없습니다. 가장 먼저 댓글을 남겨보세요!</h5>
		</c:if>

		<!-- 댓글이 있을 경우 목록 출력 -->
		<c:if test="${fn:length(commentList) > 0 }">
			<c:forEach var="commentList" items="${commentList}">

				<!-- 댓글 내용 출력 -->
				<div class="item">
					<div class="user">

						<!-- 회원 프로필 이미지 -->
						<figure>
							<img  src="profileImg/${commentList.profileImgName}" >
						</figure>

						<div class="details">
							<h5 class="title">${commentList.memberNickname}
								<c:if test="${member == null || member != commentList.memberId}">
									<a href="/memberPage?memberId=${commentList.memberId}">${commentList.memberId}</a>
								</c:if>
								<c:if test="${member == commentList.memberId}">
									<a href="/myPage?memberId=${member}">${commentList.memberId}</a>
								</c:if>
							</h5>
							<div class="time"></div>
							<div class="description">${commentList.commentContent}</div>

							<!-- 답글 달기 -->
							<footer  style="display: flex; flex-wrap: wrap;">
								<c:if test="${fn:length(replyList) > 0}">
									<a href="javascript:void(0);" onclick="toggleReply()"
										style="justify-content: left;">Reply</a>
									<div id="webReply" style="display: none;">
										<webComments:webReply />
									</div>
								</c:if> 

								<!-- 미 로그인 시 경고 버튼 -->
								<c:if test="${member == null}">
									<input class="btn btn-primary btn-rounded" type="button" style="width : 25%;"
										value="Reply" onclick="message()" />
								</c:if>
								
								<!-- 작성자가 자기 댓글을 수정할 경우 -->
								<c:if test="${commentList.memberId == member}">
									<input class="btn btn-primary btn-rounded" type="button" style="width : 25%; margin-right: 10px;"
										value="Update" id="replyUpdateBtn" onclick="toggleUpdate()">

									<input class="btn btn-primary btn-rounded" type="button" style="width : 25%; margin-right: 10px;"
										value="Delete" id="replyDeleteBtn" onclick="toggleDelete()">
								</c:if>
								
								<!-- 답글 작성 버튼 클릭 이벤트 -->
								<c:if test="${member != null}">
									<input class="btn btn-primary btn-rounded" type="button"  style="width : 25%;"
										value="Reply" onclick="toggleReplyWrite(this)" data-comment-id="${commentList.commentId}"/>
										<!-- 답글 입력 -->
										<div class="updateContents" id="writeReplyForm-${commentList.commentId}"
											style="display: none; flex-basis: 100%;">
											<form action="/writeReply" method="post" class="row">
												<div class="form-group col-md-12" style="text-align: left;">
													<label for="message">Reply <span class="required"></span></label>
													<textarea class="form-control" id="commentContent"
														name="commentContent" placeholder="Write your Reply ..."></textarea>
												</div>
												<div class="form-group col-md-12" style="text-align: right;">
													<button class="btn btn-primary">Send Reply</button>
												</div>
											</form>
										</div>
								</c:if>

							</footer>
						</div>
					</div>
				</div>
			</c:forEach>
		</c:if>

	</div>

</div>


<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>

// 댓글 작성
$(document).ready(function() {
    $('#commentForm').submit(function(event) {
        event.preventDefault(); // 폼의 기본 제출 동작 방지

        var formData = $(this).serialize(); // 폼 데이터 직렬화

        $.ajax({
            type: "POST",
            url: "/writeComment",
            data: formData,
            success: function(response) {
                // 댓글 작성 성공 시 동작
                swal("success", "댓글이 성공적으로 작성되었습니다.", "success", {
					button : "OK",
				});
                history.go(0);
                // 성공 시 추가적인 동작, 예를 들어 댓글 목록 업데이트 등
            },
            error: function() {
                // 댓글 작성 실패 시 동작
                swal("fail", "댓글 작성에 실패했습니다.", "error", {
					button : "OK",
				});
            }
        });
    });
});

	//토글 기능을 수행하는 JavaScript 함수
	function toggleReply() {
		var replyElement = document.getElementById("webReply");
		if (replyElement.style.display === "none") {
			replyElement.style.display = "block"; // 요소를 보여줍니다.
		} else {
			replyElement.style.display = "none"; // 요소를 숨깁니다.
		}
	}

	function toggleWrite() {
		$("#writeResponseForm").toggle(); // jQuery를 사용하여 토글
	}
	function toggleReplyWrite(element) {
		 // 클릭된 답글 버튼에서 data-comment-id 속성 값을 받아온다.
	    var commentId = $(element).data('comment-id');

	    // 해당 댓글 ID에 기반한 위치에 답글 입력창을 표시
	    //입력창을 해당 댓글의 바로 아래에 표시하고 싶다면, 해당 댓글의 마크업 구조에 따라 적절한 선택자를 사용하여 위치를 지정
	   	$("#writeReplyForm-" + commentId).toggle();
	}

	function message() {
		swal("fail", "로그인 후 이용해주세요.", "error", {
			button : "OK",
		});
	}

	function toggleReplies(commentId) {
		var replyList = document.getElementById("replyList-" + commentId);
		if (replyList.style.display === "none") {
			replyList.style.display = "block";
		} else {
			replyList.style.display = "none";
		}
	}

	//현재 시간
	const now = new Date();

	function toggleEdit(commentId, event) {

		// 클릭된 요소
		var clickedElement = $(event.target);

		// 수정을 클릭한 경우
		if (clickedElement.hasClass('updateContents')) {
			// '수정'인 경우
			if (clickedElement.val() === 'replyMoreBtn') {
				// '수정'을 '저장'으로 변경
				clickedElement.text('Save');

				// 해당 댓글Div에 있는 내용을 divContent에 저장
				var divContent = $('#reviewContents_' + reviewNum).text();

				// 텍스트 영역 엘리먼트 생성
				var textarea = $('<textarea>').val(divContent.trim()).attr(
						'rows', 5);

				// div 엘리먼트의 내용을 텍스트 영역으로 완전히 대체
				var divElement = $('#commentContent_' + commentId);

				if (divElement.length) {
					// 선택된 div 요소를 비우고, 그 안에 새로운 textarea 요소를 추가
					divElement.empty().append(textarea);
				}
			}
			// '수정'이 아닌 경우
			else {

				// 텍스트 영역에서 업데이트된 내용 가져오기
				var updatedContents = $(
						'#commentContent_' + commentId + ' textarea').val();

				// 유효성 검사: 공백이나 내용이 없는 경우 저장하지 않음
				if (updatedContents.trim() === '') {
					alert('내용을 입력하세요.'); // 또는 다른 사용자 피드백 방식으로 변경
					return; // 저장 중단
				}

				// AJAX 요청 수행
				$.ajax({
					url : "/updateComment", // 실제 서버 엔드포인트로 변경
					type : "POST", // 요청 방식 (GET, POST 등)
					data : {
						// update를 하기 위한 요소들을 보냄
						'commentId' : commentId,
						'commentContent' : updatedContents
					},
					dataType : 'text',
					success : function(response) {
						// response 확인
						// console.log('서버 응답:' + response);

						// 성공적으로 업데이트된 경우
						// div 엘리먼트의 내용을 업데이트된 내용으로 교체
						var commentContentsDiv = $('#commentContent_'
								+ commentId);
						if (commentContentsDiv.length) {
							commentContentsDiv.text(response);
							// 현재 db에 저장된 데이터
							// 서버에서 응답을 줄 때 인자인 Response에 태워서 보내줘야함
						}
						// '저장'을 '수정'으로 변경

						clickedElement.text('Update');

					},
					error : function(error) {
						console.error('에러 발생:', error);
						// 에러 처리 로직 추가
					}
				});

				// 이벤트 전파 막기
				event.stopPropagation();
			}
		}
	}
</script>