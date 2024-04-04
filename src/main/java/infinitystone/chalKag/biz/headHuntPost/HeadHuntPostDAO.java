package infinitystone.chalKag.biz.headHuntPost;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository("headHuntPostDAO")
public class HeadHuntPostDAO { // 구인 게시판 DAO

  @Autowired
  private JdbcTemplate jdbcTemplate;
  
// ----------------------------------------------------------------- 메인 페이지 SELECTALL -----------------------------------------------------------------

 // 프리미엄 회원이 작성한 구인글 출력 (최신글 2개만 출력).전미지
 private static final String SELECTALL_HEADHUNTPOSTPREMIUM = "SELECT "
		  + "	'HeadHuntPost' AS POST_category, " // 게시판 카테고리 설정
		  + "	HEADHUNTPOST.HEADHUNTPOST_id, "
		  + "	MEMBER.MEMBER_nickname, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_title, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_content, "
		  + "	( " // 대표 이미지 설정
		  + "		SELECT "
		  + "			POSTIMG.POSTIMG_name " // 게시글 이미지를 선택
		  + "		FROM "
		  + "			POSTIMG " // 게시글 이미지 테이블
		  + "		WHERE "
		  + "			POSTIMG.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 이미지 테이블의 게시글 아이디가 동일한 것을 선택
		  + "		ORDER BY "
		  + "			POSTIMG.POSTIMG_id ASC " // 이미지 아이디를 기준으로 오름차순 정렬
		  + "		LIMIT 1 " // 이미지를 1개만 가져오도록 설정
		  + "	 ) AS POSTIMG_name " // 대표 이미지의 이름
		  + "FROM "
		  + "	HEADHUNTPOST " // 구인글 테이블
		  + "INNER JOIN "
		  + "	MEMBER ON HEADHUNTPOST.MEMBER_id = MEMBER.MEMBER_id " // 회원 정보와 INNER JOIN
		  + "LEFT JOIN "
		  + "	RECOMMEND ON HEADHUNTPOST.HEADHUNTPOST_id = RECOMMEND.POST_id " // 좋아요 정보와 LEFT JOIN
		  + "WHERE "
		  + "    MEMBER.MEMBER_grade = 'PREMIUM' "
		  + "ORDER BY "
		  + "   HEADHUNTPOST.HEADHUNTPOST_date DESC " // 작성일을 기준으로 내림차순 정렬
		  + "LIMIT 2 "; // 글을 2개만 가져오도록 설정
 // 사용한 테이블 : 구인글 테이블, 회원 테이블, 좋아요 테이블, 게시글 이미지 테이블
 // 사용한 컬럼 (출력 내용) :
 // 게시글 카테고리, 회원 닉네임(회원 테이블), 구인글 제목, 구인글 내용, 게시글 이미지 테이블(게시글 이미지 테이블)
 // 쿼리문 설명 :
 // INNER JOIN을 사용해 구인글 테이블과 회원 테이블을 연결하고, 또 다른 LEFT JOIN을 사용해 구인글 테이블과 좋아요 테이블을 연결
 // 게시글 이미지는 테이블을 따로 나누었으며 서브 쿼리를 사용해 게시글 이미지 중 대표 이미지로 보여줄 이미지를 설정하고, 그 결과를 "POSTIMG_name"라는 별칭으로 반환
 
 
 // 주간 추천순 구인글 목록 출력 (2개만 출력).전미지
 private static final String SELECTALL_HEADHUNTPOSTWEEKLYBEST = "SELECT "
		  + "	'HeadHuntPost' AS POST_category, " // 게시판 카테고리 설정
		  + "	HEADHUNTPOST.HEADHUNTPOST_id, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_title, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_content, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_date, "
		  + "	( " // 게시글의 좋아요 수를 합산
		  + "		SELECT "
		  + "            COUNT(*) " // 해당 게시글에 대한 좋아요 수를 COUNT 함수를 사용해 합산
		  + "		FROM "
		  + "            RECOMMEND " // 좋아요 테이블에서 가져옴
		  + "		WHERE "
		  + "            RECOMMEND.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 좋아요 테이블의 게시글 아이디가 동일한 것을 선택
		  + "	) AS RECOMMEND_cnt, " // 좋아요 수
		  + "	( " // 대표 이미지 설정
		  + "		SELECT "
		  + "			POSTIMG.POSTIMG_name " // 게시글 이미지를 선택
		  + "		FROM "
		  + "			POSTIMG " // 게시글 이미지 테이블
		  + "		WHERE "
		  + "			POSTIMG.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 이미지 테이블의 게시글 아이디가 동일한 것을 선택
		  + "		ORDER BY "
		  + "			POSTIMG.POSTIMG_id ASC " // 이미지 아이디를 기준으로 오름차순 정렬
		  + "		LIMIT 1 " // 이미지를 1개만 가져오도록 설정
		  + "	 ) AS POSTIMG_name " // 대표 이미지의 이름
		  + "FROM "
		  + "	HEADHUNTPOST " // 구인글 테이블
		  + "LEFT JOIN "
		  + "	RECOMMEND ON HEADHUNTPOST.HEADHUNTPOST_id = RECOMMEND.POST_id " // 좋아요 정보와 LEFT JOIN
		  + "WHERE "
		  + "	HEADHUNTPOST.HEADHUNTPOST_date >= DATE_SUB(NOW(), INTERVAL1 WEEK), "
		  + "GROUP BY "
		  + "	HEADHUNTPOST.HEADHUNTPOST_id "
		  + "ORDER BY "
		  + "	COUNT(RECOMMEND.POST_id) DESC, " // (1) 좋아요 수가 많은 순으로 정렬한 뒤
		  + "	HEADHUNTPOST.HEADHUNTPOST_date DESC, " // (2) 다음 작성일이 최신인 순으로 정렬
		  + "LIMIT 2 ";
 // 사용한 테이블 : 구인글 테이블, 회원 테이블, 좋아요 테이블, 게시글 이미지 테이블
 // 사용한 컬럼 (출력 내용) :
 // 게시글 카테고리, 구인글 아이디, 구인글 제목, 구인글 내용, 구인글 작성일, 게시글의 좋아요 수(좋아요 테이블), 게시글 이미지 테이블(게시글 이미지 테이블)
 // 쿼리문 설명 :
 // INNER JOIN을 사용해 구인글 테이블과 회원 테이블을 연결하고, 또 다른 LEFT JOIN을 사용해 구인글 테이블과 좋아요 테이블을 연결
 // 게시글의 좋아요는 테이블을 따로 나누었으며 COUNT() 함수를 사용해 게시글에 대한 좋아요 수를 합산하고, 그 결과를 "RECOMMEND_cnt"라는 별칭으로 반환
 // 게시글 이미지는 테이블을 따로 나누었으며 서브 쿼리를 사용해 게시글 이미지 중 대표 이미지로 보여줄 이미지를 설정하고, 그 결과를 "POSTIMG_name"라는 별칭으로 반환
 
 
	// 작성 시간 경과를 알려주는 구인글 목록 출력.전미지 (사용하지 않을듯)
//private static final String SELECTALL_HEADHUNTPOSTTIME= "SELECT "
//		  + "	'HeadHuntPost' AS POST_category, " // 게시판 카테고리 설정
//		  + "	HEADHUNTPOST.HEADHUNTPOST_id, "
//		  + "	HEADHUNTPOST.MEMBER_id, "
//		  + "	MEMBER.MEMBER_nickname, "
//		  + "	HEADHUNTPOST.HEADHUNTPOST_title, "
//		  + "	HEADHUNTPOST.HEADHUNTPOST_content, "
//		  + "	HEADHUNTPOST.HEADHUNTPOST_date, "
//		  + "	HEADHUNTPOST.HEADHUNTPOST_viewcnt, "
//		  + "	( " // 대표 이미지 설정
//		  + "		SELECT "
//		  + "			POSTIMG.POSTIMG_name " // 게시글 이미지를 선택
//		  + "		FROM "
//		  + "			POSTIMG " // 게시글 이미지 테이블
//		  + "		WHERE "
//		  + "			POSTIMG.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 이미지 테이블의 게시글 아이디가 동일한 것을 선택
//		  + "		ORDER BY "
//		  + "			POSTIMG.POSTIMG_id ASC " // 이미지 아이디를 기준으로 오름차순 정렬
//		  + "		LIMIT 1 " // 이미지를 1개만 가져오도록 설정
//		  + "	 ) AS POSTIMG_name, " // 대표 이미지의 이름
//		  + "	( " // 게시글의 좋아요 수를 합산
//		  + "		SELECT "
//		  + "            COUNT(*) " // 해당 게시글에 대한 좋아요 수를 COUNT 함수를 사용해 합산
//		  + "		FROM "
//		  + "            RECOMMEND " // 좋아요 테이블에서 가져옴
//		  + "		WHERE "
//		  + "            RECOMMEND.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 좋아요 테이블의 게시글 아이디가 동일한 것을 선택
//		  + "	) AS RECOMMEND_cnt, " // 좋아요 수
//		  + "	CASE "
//		  + "  		WHEN TIMESTAMPDIFF(MINUTE, HEADHUNTPOST.HEADHUNTPOST_date, NOW()) < 60 THEN CONCAT "
//		  + "  			(TIMESTAMPDIFF(MINUTE, HEADHUNTPOST.HEADHUNTPOST_date, NOW()), ' 분 전') "
//		  + "  		WHEN TIMESTAMPDIFF(HOUR, HEADHUNTPOST.HEADHUNTPOST_date, NOW()) < 24 THEN CONCAT "
//		  + "  			(TIMESTAMPDIFF(HOUR, HEADHUNTPOST.HEADHUNTPOST_date, NOW()), ' 시간 전')  "
//		  + "		ELSE CONCAT(TIMESTAMPDIFF(DAY, HEADHUNTPOST.HEADHUNTPOST_date, NOW()), ' 일 전')  "
//		  + "	END AS HEADHUNTPOST_date"
//		  + "FROM "
//		  + "	HEADHUNTPOST " // 구인글 테이블
//		  + "INNER JOIN "
//		  + "	MEMBER ON HEADHUNTPOST.MEMBER_id = MEMBER.MEMBER_id " // 회원 정보와 INNER JOIN
//		  + "LEFT JOIN "
//		  + "	RECOMMEND ON HEADHUNTPOST.HEADHUNTPOST_id = RECOMMEND.POST_id " // 좋아요 정보와 LEFT JOIN
//		  + "ORDER BY "
//		  + "    HEADHUNTPOST.HEADHUNTPOST_date DESC " // 작성일을 기준으로 내림차순 정렬
//		  + "LIMIT 2 "; // 구인글을 2개만 가져오도록 설정
// 사용한 테이블 : 구인글 테이블, 회원 테이블, 좋아요 테이블, 게시글 이미지 테이블
// 사용한 컬럼 (출력 내용) :
// 게시글 카테고리, 구인글 아이디, 회원 아이디, 회원 닉네임(회원 테이블), 구인글 제목, 구인글 내용,
// 구인글 작성일, 구인글 조회수, 게시글 이미지 테이블(게시글 이미지 테이블), 게시글의 좋아요 수(좋아요 테이블)
// 쿼리문 설명 :
// INNER JOIN을 사용해 구인글 테이블과 회원 테이블을 연결하고, 또 다른 LEFT JOIN을 사용해 구인글 테이블과 좋아요 테이블을 연결
// 게시글 이미지는 테이블을 따로 나누었으며 서브 쿼리를 사용해 게시글 이미지 중 대표 이미지로 보여줄 이미지를 설정하고, 그 결과를 "POSTIMG_name"라는 별칭으로 반환
// 게시글의 좋아요는 테이블을 따로 나누었으며 COUNT() 함수를 사용해 게시글에 대한 좋아요 수를 합산하고, 그 결과를 "RECOMMEND_cnt"라는 별칭으로 반환
 
// ----------------------------------------------------------------- 구인글 페이지 SELECTALL -----------------------------------------------------------------
 
 // 프리미엄 회원이 작성한지 한 달 이내의 글 목록 출력.전미지
 private static final String SELECTALL_HEADHUNTPOSTPREMIUM1MONTH = "SELECT "
		  + "	'HeadHuntPost' AS POST_category, " // 게시판 카테고리 설정
		  + "	HEADHUNTPOST.HEADHUNTPOST_id, "
		  + "	HEADHUNTPOST.MEMBER_id, "
		  + "	MEMBER.MEMBER_nickname, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_title, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_content, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_date, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_viewcnt, "
		  + "	( " // 대표 이미지 설정
		  + "		SELECT "
		  + "			POSTIMG.POSTIMG_name " // 게시글 이미지를 선택
		  + "		FROM "
		  + "			POSTIMG " // 게시글 이미지 테이블
		  + "		WHERE "
		  + "			POSTIMG.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 이미지 테이블의 게시글 아이디가 동일한 것을 선택
		  + "		ORDER BY "
		  + "			POSTIMG.POSTIMG_id ASC " // 이미지 아이디를 기준으로 오름차순 정렬
		  + "		LIMIT 1 " // 이미지를 1개만 가져오도록 설정
		  + "	 ) AS POSTIMG_name, " // 대표 이미지의 이름
		  + "	( " // 게시글의 좋아요 수를 합산
		  + "		SELECT "
		  + "            COUNT(*) " // 해당 게시글에 대한 좋아요 수를 COUNT 함수를 사용해 합산
		  + "		FROM "
		  + "            RECOMMEND " // 좋아요 테이블에서 가져옴
		  + "		WHERE "
		  + "            RECOMMEND.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 좋아요 테이블의 게시글 아이디가 동일한 것을 선택
		  + "	) AS RECOMMEND_cnt, " // 좋아요 수
		  + "FROM "
		  + "	HEADHUNTPOST " // 구인글 테이블
		  + "INNER JOIN "
		  + "	MEMBER ON HEADHUNTPOST.MEMBER_id = MEMBER.MEMBER_id " // 회원 정보와 INNER JOIN
		  + "LEFT JOIN "
		  + "	RECOMMEND ON HEADHUNTPOST.HEADHUNTPOST_id = RECOMMEND.POST_id " // 좋아요 정보와 LEFT JOIN
		  + "WHERE "
		  + "    MEMBER.MEMBER_grade = 'PREMIUM' " // 회원 등급이 'PREMIUM' 회원 이면서
		  + "	AND HEADHUNTPOST.HEADHUNTPOST_date >= DATE_SUB(NOW(), INTERVAL 1 MONTH) " // 작성일이 한 달 이내인 경우
		  + "ORDER BY "
		  + "   HEADHUNTPOST.HEADHUNTPOST_date DESC "; // 작성일을 기준으로 내림차순 정렬
 // 사용한 테이블 : 구인글 테이블, 회원 테이블, 좋아요 테이블, 게시글 이미지 테이블
 // 사용한 컬럼 (출력 내용) :
 // 게시글 카테고리, 회원 닉네임(회원 테이블), 구인글 제목, 구인글 내용, 게시글 이미지 테이블(게시글 이미지 테이블)
 // 쿼리문 설명 :
 // INNER JOIN을 사용해 구인글 테이블과 회원 테이블을 연결하고, 또 다른 LEFT JOIN을 사용해 구인글 테이블과 좋아요 테이블을 연결
 // 게시글 이미지는 테이블을 따로 나누었으며 서브 쿼리를 사용해 게시글 이미지 중 대표 이미지로 보여줄 이미지를 설정하고, 그 결과를 "POSTIMG_name"라는 별칭으로 반환

 
  // 구인글 목록 출력.전미지
  private static final String SELECTALL_HEADHUNTPOST = "SELECT "
		  + "	'HeadHuntPost' AS POST_category, " // 게시판 카테고리 설정
		  + "	HEADHUNTPOST.HEADHUNTPOST_id, "
		  + "	HEADHUNTPOST.MEMBER_id, "
		  + "	MEMBER.MEMBER_nickname, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_title, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_content, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_date, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_viewcnt, "
		  + "	( " // 게시글의 좋아요 수를 합산
		  + "		SELECT "
		  + "            COUNT(*) " // 해당 게시글에 대한 좋아요 수를 COUNT 함수를 사용해 합산
		  + "		FROM "
		  + "            RECOMMEND " // 좋아요 테이블에서 가져옴
		  + "		WHERE "
		  + "            RECOMMEND.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 좋아요 테이블의 게시글 아이디가 동일한 것을 선택
		  + "	) AS RECOMMEND_cnt, " // 좋아요 수
		  + "	( " // 대표 이미지 설정
		  + "		SELECT "
		  + "			POSTIMG.POSTIMG_name " // 게시글 이미지를 선택
		  + "		FROM "
		  + "			POSTIMG " // 게시글 이미지 테이블
		  + "		WHERE "
		  + "			POSTIMG.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 이미지 테이블의 게시글 아이디가 동일한 것을 선택
		  + "		ORDER BY "
		  + "			POSTIMG.POSTIMG_id ASC " // 이미지 아이디를 기준으로 오름차순 정렬
		  + "		LIMIT 1 " // 이미지를 1개만 가져오도록 설정
		  + "	 ) AS POSTIMG_name " // 대표 이미지의 이름
		  + "FROM "
		  + "	HEADHUNTPOST " // 구인글 테이블
		  + "INNER JOIN "
		  + "	MEMBER ON HEADHUNTPOST.MEMBER_id = MEMBER.MEMBER_id " // 회원 정보와 INNER JOIN
		  + "LEFT JOIN "
		  + "	RECOMMEND ON HEADHUNTPOST.HEADHUNTPOST_id = RECOMMEND.POST_id " // 좋아요 정보와 LEFT JOIN
		  + "ORDER BY "
		  + "	HEADHUNTPOST.HEADHUNTPOST_id DESC"; // 게시글 아이디를 기준으로 내림차순 정렬
  // 사용한 테이블 : 구인글 테이블, 회원 테이블, 좋아요 테이블, 게시글 이미지 테이블
  // 사용한 컬럼 (출력 내용) :
  // 게시글 카테고리, 구인글 아이디, 회원 닉네임(회원 테이블), 구인글 제목, 구인글 내용,
  // 구인글 작성일, 구인글 조회수, 게시글의 좋아요 수(좋아요 테이블), 게시글 이미지 테이블(게시글 이미지 테이블)
  // 쿼리문 설명 :
  // INNER JOIN을 사용해 구인글 테이블과 회원 테이블을 연결하고, 또 다른 LEFT JOIN을 사용해 구인글 테이블과 좋아요 테이블을 연결
  // 게시글의 좋아요는 테이블을 따로 나누었으며 COUNT() 함수를 사용해 게시글에 대한 좋아요 수를 합산하고, 그 결과를 "RECOMMEND_cnt"라는 별칭으로 반환
  // 게시글 이미지는 테이블을 따로 나누었으며 서브 쿼리를 사용해 게시글 이미지 중 대표 이미지로 보여줄 이미지를 설정하고, 그 결과를 "POSTIMG_name"라는 별칭으로 반환

// ----------------------------------------------------------------- 회원 페이지 SELECTALL -----------------------------------------------------------------
  
  // 특정 회원이 작성한 구인글 목록 출력.전미지
  private static final String SELECTALL_HEADHUNTPOSTMEMBER = "SELECT "
		  + "	'HeadHuntPost' AS POST_category, " // 게시판 카테고리 설정
		  + "	HEADHUNTPOST.HEADHUNTPOST_id, "
		  + "	MEMBER.MEMBER_nickname, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_title, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_content, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_date, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_viewcnt, "
		  + "	( " // 게시글의 좋아요 수를 합산
		  + "		SELECT "
		  + "            COUNT(*) " // 해당 게시글에 대한 좋아요 수를 COUNT 함수를 사용해 합산
		  + "		FROM "
		  + "            RECOMMEND " // 좋아요 테이블에서 가져옴
		  + "		WHERE "
		  + "            RECOMMEND.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 좋아요 테이블의 게시글 아이디가 동일한 것을 선택
		  + "	) AS RECOMMEND_cnt, " // 좋아요 수
		  + "	( " // 대표 이미지 설정
		  + "		SELECT "
		  + "			POSTIMG.POSTIMG_name " // 게시글 이미지를 선택
		  + "		FROM "
		  + "			POSTIMG " // 게시글 이미지 테이블
		  + "		WHERE "
		  + "			POSTIMG.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 이미지 테이블의 게시글 아이디가 동일한 것을 선택
		  + "		ORDER BY "
		  + "			POSTIMG.POSTIMG_id ASC " // 이미지 아이디를 기준으로 오름차순 정렬
		  + "		LIMIT 1 " // 이미지를 1개만 가져오도록 설정
		  + "	 ) AS POSTIMG_name " // 대표 이미지의 이름
		  + "FROM "
		  + "	HEADHUNTPOST " // 구인글 테이블
		  + "INNER JOIN "
		  + "	MEMBER ON HEADHUNTPOST.MEMBER_id = MEMBER.MEMBER_id " // 회원 정보와 INNER JOIN
		  + "LEFT JOIN "
		  + "	RECOMMEND ON HEADHUNTPOST.HEADHUNTPOST_id = RECOMMEND.POST_id " // 좋아요 정보와 LEFT JOIN
		  + "WHERE "
		  + "	MEMBER.MEMBER_id = ? " // 조회할 회원의 아이디
		  + "ORDER BY"
		  + "	HEADHUNTPOST.HEADHUNTPOST_id DESC"; // 게시글 아이디를 기준으로 내림차순 정렬
  // 사용한 테이블 : 구인글 테이블, 회원 테이블, 좋아요 테이블, 게시글 이미지 테이블
  // 사용한 컬럼 (출력 내용) :
  // 게시글 카테고리, 구인글 아이디, 회원 닉네임(회원 테이블), 구인글 제목, 구인글 내용,
  // 구인글 작성일, 구인글 조회수, 게시글의 좋아요 수(좋아요 테이블), 게시글 이미지 테이블(게시글 이미지 테이블)
  // 쿼리문 설명 :
  // INNER JOIN을 사용해 구인글 테이블과 회원 테이블을 연결하고, 또 다른 LEFT JOIN을 사용해 구인글 테이블과 좋아요 테이블을 연결
  // 게시글의 좋아요는 테이블을 따로 나누었으며 COUNT() 함수를 사용해 게시글에 대한 좋아요 수를 합산하고, 그 결과를 "RECOMMEND_cnt"라는 별칭으로 반환
  // 게시글 이미지는 테이블을 따로 나누었으며 서브 쿼리를 사용해 게시글 이미지 중 대표 이미지로 보여줄 이미지를 설정하고, 그 결과를 "POSTIMG_name"라는 별칭으로 반환
 
// ---------------------------------------------------------------------- SELECTONE ----------------------------------------------------------------------
  
  // 게시글 아이디 최대값 가져오는 쿼리문
  private static final String SELECTONE_MAXPOSTID = "SELECT MAX(HEADHUNTPOST_id) FROM HEADHUNTPOST";
  
  
  // 메인 페이지 - 최신 게시글 1개 출력.전미지
  private static final String SELECTONE_HEADHUNTPOSTRECENT= "SELECT "
		  + "	'HeadHuntPost' AS POST_category, " // 게시판 카테고리 설정
		  + "	HEADHUNTPOST.HEADHUNTPOST_id, "	
		  + "	HEADHUNTPOST.HEADHUNTPOST_title, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_content, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_date, "
		  + "	( " // 게시글의 좋아요 수를 합산
		  + "		SELECT "
		  + "            COUNT(*) " // 해당 게시글에 대한 좋아요 수를 COUNT 함수를 사용해 합산
		  + "		FROM "
		  + "            RECOMMEND " // 좋아요 테이블에서 가져옴
		  + "		WHERE "
		  + "            RECOMMEND.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 좋아요 테이블의 게시글 아이디가 동일한 것을 선택
		  + "	) AS RECOMMEND_cnt, " // 좋아요 수
		  + "	( " // 대표 이미지 설정
		  + "		SELECT "
		  + "			POSTIMG.POSTIMG_name " // 게시글 이미지를 선택
		  + "		FROM "
		  + "			POSTIMG " // 게시글 이미지 테이블
		  + "		WHERE "
		  + "			POSTIMG.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 이미지 테이블의 게시글 아이디가 동일한 것을 선택
		  + "		ORDER BY "
		  + "			POSTIMG.POSTIMG_id ASC " // 이미지 아이디를 기준으로 오름차순 정렬
		  + "		LIMIT 1 " // 이미지를 1개만 가져오도록 설정
		  + "	 ) AS POSTIMG_name " // 대표 이미지의 이름
		  + "FROM "
		  + "	HEADHUNTPOST " // 구인글 테이블
		  + "LEFT JOIN "
		  + "	RECOMMEND ON HEADHUNTPOST.HEADHUNTPOST_id = RECOMMEND.POST_id " // 좋아요 정보와 LEFT JOIN
		  + "ORDER BY "
		  + "    HEADHUNTPOST.HEADHUNTPOST_date DESC " // 작성일을 기준으로 내림차순 정렬
		  + "LIMIT 1 "; // 글을 1개만 가져오도록 설정
  // 사용한 테이블 : 구인글 테이블, 회원 테이블, 좋아요 테이블, 게시글 이미지 테이블
  // 사용한 컬럼 (출력 내용) :
  // 게시글 카테고리, 게시글 아이디, 구인글 제목, 구인글 내용, 구인글 작성일, 게시글의 좋아요 수(좋아요 테이블), 게시글 대표 이미지(게시글 이미지 테이블)
  // 쿼리문 설명 :
  // INNER JOIN을 사용해 구인글 테이블과 회원 테이블을 연결하고, 또 다른 LEFT JOIN을 사용해 구인글 테이블과 좋아요 테이블을 연결
  // 게시글의 좋아요는 테이블을 따로 나누었으며 COUNT() 함수를 사용해 게시글에 대한 좋아요 수를 합산하고, 그 결과를 "RECOMMEND_cnt"라는 별칭으로 반환
  // 게시글 이미지는 테이블을 따로 나누었으며 서브 쿼리를 사용해 게시글 이미지 중 대표 이미지로 보여줄 이미지를 설정하고, 그 결과를 "POSTIMG_name"라는 별칭으로 반환
  
  
  // 구인글 페이지 - 구인글 상세 출력.전미지
  private static final String SELECTONE_HEADHUNTPOST = "SELECT "
		  + "	'HeadHuntPost' AS POST_category, " // 게시판의 카테고리 설정 "
		  + "	HEADHUNTPOST.HEADHUNTPOST_id, "
		  + "	HEADHUNTPOST.MEMBER_id, "
		  + "	MEMBER.MEMBER_nickname, "
		  + "	MEMBER.MEMBER_introduction, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_title, "	
		  + "	HEADHUNTPOST.HEADHUNTPOST_content, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_role, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_region, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_pay, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_workDate, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_concept, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_date, "
		  + "	HEADHUNTPOST.HEADHUNTPOST_viewcnt, "
		  + "	( " // 회원 프로필로 보여줄 이미지 설정
		  + "		SELECT "
		  + "			PROFILEIMG.PROFILEIMG_name " // 프로필 이미지를 선택 
		  + "		FROM  "
		  + "			PROFILEIMG " // 프로필 이미지 테이블
		  + "		WHERE  "
		  + "			PROFILEIMG.MEMBER_id = HEADHUNTPOST.MEMBER_id " // 회원 아이디와 프로필 이미지 테이블의 회원 아이디가 동일한 것을 선택
		  + "		ORDER BY  "
		  + "			PROFILEIMG.PROFILEIMG_id DESC " // 프로필 이미지 아이디를 기준으로 내림차순 정렬
		  + "		LIMIT 1 " // 이미지를 1개만 가져오도록 설정
		  + "	) AS PROFILEIMG_name, " // 프로필 이미지의 이름
		  + "	( " // 게시글의 좋아요 수를 합산
		  + "		SELECT "
		  + "			COUNT(*) " // 해당 게시글에 대한 좋아요 수를 COUNT 함수를 사용해 합산
		  + "		FROM "
		  + "			RECOMMEND " // 좋아요 테이블에서 가져옴
		  + "		WHERE "
		  + "			RECOMMEND.POST_id = HEADHUNTPOST.HEADHUNTPOST_id " // 게시글 아이디와 좋아요 테이블의 게시글 아이디가 동일한 것을 선택
		  + "	) AS RECOMMEND_cnt " // 좋아요 수
		  + "FROM "
		  + "	HEADHUNTPOST " // 구인글 테이블
		  + "INNER JOIN  "
		  + "	MEMBER ON HEADHUNTPOST.MEMBER_id = MEMBER.MEMBER_id " // 회원 정보와 INNER JOIN
		  + "LEFT JOIN  "
		  + "	RECOMMEND ON HEADHUNTPOST.HEADHUNTPOST_id = RECOMMEND.POST_id " // 좋아요 정보와 LEFT JOIN
		  + "WHERE  "
		  + "	HEADHUNTPOST.HEADHUNTPOST_id = ? " ;
  // 사용한 테이블 : 구인글 테이블, 회원 테이블, 좋아요 테이블, 프로필 이미지 테이블
  // 사용한 컬럼 (출력 내용) :
  // 게시글 카테고리, 구인글 아이디, 회원 아이디, 회원 닉네임(회원 테이블), 회원 자기소개(회원 테이블), 구인글 제목, 구인글 내용,
  // 구인글 직업 (모델/사진작가), 구인글 작업 지역, 구인글 작업 페이, 구인글 작업 날짜, 구인글 작업 컨셉,
  // 구인글 작성일, 구인글 조회수, 게시글의 좋아요 수(좋아요 테이블), 프로필 이미지(프로필 이미지 테이블)
  // 쿼리문 설명 :
  // INNER JOIN을 사용해 구인글 테이블과 회원 테이블을 연결하고, 또 다른 LEFT JOIN을 사용해 구인글 테이블과 좋아요 테이블을 연결
  // 프로필 이미지는 테이블을 따로 나누었으며 서브 쿼리를 사용해 프로필 이미지 중 회원 프로필로 보여줄 이미지를 설정하고, 그 결과를 "PROFILEIMG_name"라는 별칭으로 반환
  // 게시글의 좋아요는 테이블을 따로 나누었으며 COUNT() 함수를 사용해 게시글에 대한 좋아요 수를 합산하고, 그 결과를 "RECOMMEND_cnt"라는 별칭으로 반환
  
// --------------------------------------------------------------- INSERT, UPDATE, DELETE ---------------------------------------------------------------
  
  // 구인글 작성.전미지
  private static final String INSERT_HEADHUNTPOST = "INSERT INTO HEADHUNTPOST ( "
		  + "MEMBER_id, "
		  + "HEADHUNTPOST_role, "
		  + "HEADHUNTPOST_region, "
		  + "HEADHUNTPOST_pay, "
		  + "HEADHUNTPOST_workDate, "
		  + "HEADHUNTPOST_concept, "
		  + "HEADHUNTPOST_title, "
		  + "HEADHUNTPOST_content, "
		  + "HEADHUNTPOST_viewcnt "
		  + ")VALUES(?,?,?,?,?,?,?,?,0) ";
  // 사용한 테이블 : 구인글 테이블
  // 사용한 컬럼 (작성 내용) :
  // 회원 아이디(회원 테이블), 구인글 작성 시간, 구인글 직업, 구인글 작업 지역, 구인글 작업 페이,
  // 구인글 작업 날짜, 구인글 촬영 컨셉, 구인글 제목, 구인글 내용, 구인글 조회수
  // 구인글 아이디는 테이블 생성시 AUTO_INCREMENT를 사용해 2001번부터 자동 증감하게 설정

  
  // 구인글 조회수 증가.전미지
  private static final String UPDATE_VIEWCNT = "UPDATE HEADHUNTPOST "
		  + "SET "
		  + "	HEADHUNTPOST_viewcnt = (HEADHUNTPOST_viewcnt+1) "
		  + "WHERE "
		  + "	HEADHUNTPOST_id = ? ";
  // 사용한 테이블 : 구인글 테이블
  // 사용한 컬럼 (조회수 업데이트) : 구인글 조회수
 
  
  // 구인글 수정.전미지
  private static final String UPDATE_HEADHUNTPOST = "UPDATE HEADHUNTPOST "
		  + "SET "
		  + "	HEADHUNTPOST_role = ?, "
		  + "	HEADHUNTPOST_region = ?, "
		  + "	HEADHUNTPOST_pay = ?, "
		  + "	HEADHUNTPOST_workDate = ?, "
		  + "	HEADHUNTPOST_concept = ?, "
		  + "	HEADHUNTPOST_title = ?, "
		  + "	HEADHUNTPOST_content = ? "
		  + "WHERE "
		  + "	HEADHUNTPOST_id = ? ";
  // 사용한 테이블 : 구인글 테이블
  // 사용한 컬럼 (수정 내용) :
  // 구인글 직업, 구인글 작업 지역, 구인글 작업 페이, 구인글 작업 날짜, 구인글 촬영 컨셉, 구인글 제목, 구인글 내용
  
  
  // 구인글 삭제.전미지
  private static final String DELETE_HEADHUNTPOST = "DELETE "
		  + "FROM "
		  + "      HEADHUNTPOST "
		  + "WHERE "
		  + "      HEADHUNTPOST_id = ? ";

   
  // 구인글 목록 출력
  public List<HeadHuntPostDTO> selectAll(HeadHuntPostDTO headHuntPostDTO) {
	  List<HeadHuntPostDTO> result = null;
	  // 검색 조건에 해당될 경우 jdbcTemplate을 사용하여 SELECTALL 쿼리 실행 후 결과를 RowMapper로 매핑하여 반환
	  System.out.println("HeadHuntPostDAO(selectAll) In로그 = [" + headHuntPostDTO + "]");
	  try {
		  // 메인 페이지 - 프리미엄 회원 게시글 목록 출력
		  if (headHuntPostDTO.getSearchCondition().equals("headHuntPostPremiumList")) {
			  result = jdbcTemplate.query(SELECTALL_HEADHUNTPOSTPREMIUM, new HeadHuntPostPremiumRowMapper());
			  System.out.println("HeadHuntPostDAO(selectAll) Out로그 = [" + result + "]");
			  return result;
		  }
		  // 메인 페이지 - 주간 추천순 게시글 목록 출력
		  else if (headHuntPostDTO.getSearchCondition().equals("headHuntPostWeeklyBestList")) {
			  result = jdbcTemplate.query(SELECTALL_HEADHUNTPOSTWEEKLYBEST, new HeadHuntPostWeeklyBestRowMapper());
			  System.out.println("HeadHuntPostDAO(selectAll) Out로그 = [" + result + "]");
			  return result;
		  }
		  // 구인글 페이지 - 프리미엄 회원이 작성한지 한 달 이내의 글 목록 출력
		  else if (headHuntPostDTO.getSearchCondition().equals("headHuntPostPremium1MonthList")) {
			  result = jdbcTemplate.query(SELECTALL_HEADHUNTPOSTPREMIUM1MONTH, new HeadHuntPostPremium1MonthRowMapper());
			  System.out.println("HeadHuntPostDAO(selectAll) Out로그 = [" + result + "]");
			  return result;
		  }		  
		  // 구인글 페이지 - 구인글 전체 출력
		  else if (headHuntPostDTO.getSearchCondition().equals("headHuntPostList")) {
			  result = jdbcTemplate.query(SELECTALL_HEADHUNTPOST, new HeadHuntPostRowMapper());
			  System.out.println("HeadHuntPostDAO(selectAll) Out로그 = [" + result + "]");
			  return result;
		  }
		  // 회원 페이지 - 특정 회원이 작성한 구인글 전체 출력
		  else if (headHuntPostDTO.getSearchCondition().equals("headHuntPostMembertList")) {
			  result = jdbcTemplate.query(SELECTALL_HEADHUNTPOSTMEMBER, new HeadHuntPostMemberRowMapper());
			  System.out.println("HeadHuntPostDAO(selectAll) Out로그 = [" + result + "]");
			  return result;
		  }
	  } catch (Exception e) { // 예외 발생 시
		  e.printStackTrace(); // 예외 내용 출력
		  return null; // 예외 발생 시 null 반환
	  }
	  System.out.println("HeadHuntPostDAO(selectAll) Error로그 = [" + headHuntPostDTO.getSearchCondition() + "]");
	  return null; // 글 목록 출력 조건에 해당되지 않거나 처리되지 않은 경우 null 반환
 }

	
  // 구인글 상세 출력
  public HeadHuntPostDTO selectOne(HeadHuntPostDTO headHuntPostDTO) {
	  HeadHuntPostDTO result = null;
	  System.out.println("HeadHuntPostDAO(selectOne) In로그 = [" + headHuntPostDTO + "]");
	  try {
		  Object[] args = { headHuntPostDTO.getHeadHuntPostId() };
		  // 게시글 이미지 저장 시 필요한 게시글 아이디의 최대값 가져오기
		  if (headHuntPostDTO.getSearchCondition().equals("maxPostId")) { // 게시글 작성 시 이미지에 들어갈 PostId 추가
			  result = jdbcTemplate.queryForObject(SELECTONE_MAXPOSTID, new SelectOneMaxPostIdRowMapper());
			  System.out.println("HeadHuntPostDAO(selectOne) Out로그 = [" + result + "]");
			  return result;
		  }
		  // 최신 게시글 출력
		  else if (headHuntPostDTO.getSearchCondition().equals("headHuntPostRecentPostSingle")) {
			  // SELECTONE_HEADHUNTPOST 쿼리를 실행해 데이터베이스에 구인글 데이터를 불러옴
			  result = jdbcTemplate.queryForObject(SELECTONE_HEADHUNTPOSTRECENT, new SelectOneHeadHuntPostRecentRowMapper());
			  System.out.println("HeadHuntPostDAO(selectOne) Out로그 = [" + result + "]");
			  return result;
		  }
		  // 게시글 상세 보기 
		  else if (headHuntPostDTO.getSearchCondition().equals("headHuntPostSingle")) {
			  // SELECTONE_HEADHUNTPOSTSINGLE 쿼리를 실행해 데이터베이스에 구인글 데이터를 불러옴
			  result = jdbcTemplate.queryForObject(SELECTONE_HEADHUNTPOST, args, new SelectOneHeadHuntPostRowMapper());
			  System.out.println("HeadHuntPostDAO(selectOne) Out로그 = [" + result + "]");
			  return result;
		  }
		 
	  } catch (Exception e) { // 예외 발생 시
		  e.printStackTrace(); // 예외 내용 출력
		  return null; // 예외 발생 시 null 반환
	  }
	  System.out.println("HeadHuntPostDAO(selectOne) Error로그 = [" + headHuntPostDTO.getSearchCondition() + "]");
	  return null; // 글 상세 출력 조건에 해당되지 않거나 처리되지 않은 경우 null 반환
  }

	
  // 구인글 작성
  public boolean insert(HeadHuntPostDTO headHuntPostDTO) {
	  int result = 0;
		System.out.println("HeadHuntPostDAO(insert) In로그 = [" + headHuntPostDTO + "]");
		// INSERT_HEADHUNTPOST 쿼리를 실행해 데이터베이스에 구인글 데이터를 저장
		result = jdbcTemplate.update(INSERT_HEADHUNTPOST, headHuntPostDTO.getMemberId(),
				headHuntPostDTO.getHeadHuntPostRole(), headHuntPostDTO.getHeadHuntPostRegion(),
				headHuntPostDTO.getHeadHuntPostPay(), headHuntPostDTO.getHeadHuntPostWorkDate(),
				headHuntPostDTO.getHeadHuntPostConcept(), headHuntPostDTO.getHeadHuntPostTitle(),
				headHuntPostDTO.getHeadHuntPostContent());
		if (result <= 0) {
			System.out.println("HeadHuntPostDAO(insert) Out로그 = [" + result + "]");
			return false; // 글 작성 실패 시 false 반환
		}
		return true; // 글 작성 성공 시 true 반환
	}

	
	// 구인글 조회수 증가 및 구인글 수정
	public boolean update(HeadHuntPostDTO headHuntPostDTO) {
		int result = 0;
		System.out.println("HeadHuntPostDAO(update) In로그 = [" + headHuntPostDTO + "]");
		// 구인글 조회수 증가
		if (headHuntPostDTO.getSearchCondition().equals("headHuntPostViewcntUpdate")) {
			// UPDATE_VIEWCNT 쿼리를 실행해 데이터베이스에서 구인글 조회수를 증가
			result = jdbcTemplate.update(UPDATE_VIEWCNT, headHuntPostDTO.getHeadHuntPostId());
			if (result <= 0) {
				System.out.println("HeadHuntPostDAO(update) Out로그 = [" + result + "]");
				return false; // 구인글 조회수 증가 실패 시 false 반환
			}
			return true; // 조회수 증가 성공 시 true 반환
		}
		// 구인글 수정
		else if (headHuntPostDTO.getSearchCondition().equals("headHuntPostUpdate")) {
			// UPDATE_HEADHUNTPOST 쿼리를 실행해 데이터베이스에서 구인글 정보를 수정
			result = jdbcTemplate.update(UPDATE_HEADHUNTPOST, headHuntPostDTO.getHeadHuntPostRole(),
					headHuntPostDTO.getHeadHuntPostRegion(), headHuntPostDTO.getHeadHuntPostPay(),
					headHuntPostDTO.getHeadHuntPostWorkDate(), headHuntPostDTO.getHeadHuntPostConcept(),
					headHuntPostDTO.getHeadHuntPostTitle(), headHuntPostDTO.getHeadHuntPostContent());
			if (result <= 0) {
				System.out.println("HeadHuntPostDAO(update) Out로그 = [" + result + "]");
				return false; // 구인글 수정 실패 시 false 반환
			}
			return true; // 구인글 수정 성공 시 true 반환
		}
		System.out.println("HeadHuntPostDAO(update) Error 로그 = [" + headHuntPostDTO.getSearchCondition() + "]");
		return false; // 업데이트 조건에 해당되지 않는다면 false 반환
	}

	
	// 구인글 삭제
	public boolean delete(HeadHuntPostDTO headHuntPostDTO) {
		int result = 0;
		System.out.println("HeadHuntPostDAO(delete) In로그 = [" + headHuntPostDTO + "]");
		// DELETE_HEADHUNTPOST 쿼리를 실행해 데이터베이스에서 구인글 데이터를 삭제
		result = jdbcTemplate.update(DELETE_HEADHUNTPOST, headHuntPostDTO.getHeadHuntPostId());
		if (result <= 0) {
			System.out.println("HeadHuntPostDAO(delete) Out로그 = [" + result + "]");
			return false; // 구인글 삭제 실패 시 false 반환
		}
		return true; // 구인글 삭제 성공 시 true 반환
	}

}

//---------------------------------------------------------------------- SELECTALL ----------------------------------------------------------------------

// 메인 페이지 - 프리미엄 회원이 작성한 구인글 목록 출력 시 필요한 데이터를 저장할 RowMapper 클래스.전미지
class HeadHuntPostPremiumRowMapper implements RowMapper<HeadHuntPostDTO> {
	@Override // mapRow 메서드 오버라이드
	public HeadHuntPostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 매핑(저장)하는 메서드

		HeadHuntPostDTO headHuntPostDTO = new HeadHuntPostDTO(); // 새로운 HeadHuntPostDTO 객체 생성
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 저장
		headHuntPostDTO.setPostCategory(rs.getString("POST_category"));            		// 게시글 카테고리
		headHuntPostDTO.setHeadHuntPostId(rs.getString("HEADHUNTPOST_id")); 			// 구인글 아이디
		headHuntPostDTO.setMemberNickname(rs.getString("MEMBER_nickname"));				// 회원 닉네임
		headHuntPostDTO.setHeadHuntPostTitle(rs.getString("HEADHUNTPOST_title")); 		// 구인글 제목
		headHuntPostDTO.setHeadHuntPostContent(rs.getString("HEADHUNTPOST_content")); 	// 구인글 내용
		headHuntPostDTO.setPostImgName(rs.getString("POSTIMG_name"));					// 게시글 대표 이미지
		return headHuntPostDTO; // headHuntPostDTO에 저장된 데이터들을 반환
	}
}

// 메인 페이지 - 주간 추천순 구인글 목록 출력 시 필요한 데이터를 저장할 RowMapper 클래스.전미지
class HeadHuntPostWeeklyBestRowMapper implements RowMapper<HeadHuntPostDTO> {
	@Override // mapRow 메서드 오버라이드
	public HeadHuntPostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 매핑(저장)하는 메서드

		HeadHuntPostDTO headHuntPostDTO = new HeadHuntPostDTO(); // 새로운 HeadHuntPostDTO 객체 생성
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 저장
		headHuntPostDTO.setPostCategory(rs.getString("POST_category"));            		// 게시글 카테고리
		headHuntPostDTO.setHeadHuntPostId(rs.getString("HEADHUNTPOST_id")); 			// 구인글 아이디
		headHuntPostDTO.setHeadHuntPostTitle(rs.getString("HEADHUNTPOST_title")); 		// 구인글 제목
		headHuntPostDTO.setHeadHuntPostContent(rs.getString("HEADHUNTPOST_content")); 	// 구인글 내용
		headHuntPostDTO.setHeadHuntPostDate(rs.getString("HEADHUNTPOST_date")); 		// 구인글 작성일
		headHuntPostDTO.setRecommendCnt(rs.getString("RECOMMEND_cnt")); 				// 게시글의 좋아요 수
		headHuntPostDTO.setPostImgName(rs.getString("POSTIMG_name"));					// 게시글 대표 이미지
		return headHuntPostDTO; // headHuntPostDTO에 저장된 데이터들을 반환
	}
}

// 구인글 페이지 - 프리미엄 회원이 작성한지 한 달 이내의 글 필요한 데이터를 저장할 RowMapper 클래스.전미지
class HeadHuntPostPremium1MonthRowMapper implements RowMapper<HeadHuntPostDTO> {
	@Override // mapRow 메서드 오버라이드
	public HeadHuntPostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 매핑(저장)하는 메서드

		HeadHuntPostDTO headHuntPostDTO = new HeadHuntPostDTO(); // 새로운 HeadHuntPostDTO 객체 생성
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 저장
		headHuntPostDTO.setPostCategory(rs.getString("POST_category"));            		// 게시글 카테고리
		headHuntPostDTO.setHeadHuntPostId(rs.getString("HEADHUNTPOST_id")); 			// 구인글 아이디
		headHuntPostDTO.setMemberId(rs.getString("MEMBER_id"));							// 회원 아이디
		headHuntPostDTO.setMemberNickname(rs.getString("MEMBER_nickname"));				// 회원 닉네임
		headHuntPostDTO.setHeadHuntPostTitle(rs.getString("HEADHUNTPOST_title")); 		// 구인글 제목
		headHuntPostDTO.setHeadHuntPostContent(rs.getString("HEADHUNTPOST_content")); 	// 구인글 내용
		headHuntPostDTO.setHeadHuntPostDate(rs.getString("HEADHUNTPOST_date")); 		// 구인글 작성일
		headHuntPostDTO.setHeadHuntPostViewcnt(rs.getString("HEADHUNTPOST_viewcnt")); 	// 구인글 조회수
		headHuntPostDTO.setRecommendCnt(rs.getString("RECOMMEND_cnt")); 				// 게시글의 좋아요 수
		headHuntPostDTO.setPostImgName(rs.getString("POSTIMG_name"));					// 게시글 대표 이미지
		return headHuntPostDTO; // headHuntPostDTO에 저장된 데이터들을 반환
	}
}

// 구인글 페이지 - 구인글 목록 출력 시 필요한 데이터를 저장할 RowMapper 클래스.전미지
class HeadHuntPostRowMapper implements RowMapper<HeadHuntPostDTO> {
	@Override // mapRow 메서드 오버라이드
	public HeadHuntPostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 매핑(저장)하는 메서드

		HeadHuntPostDTO headHuntPostDTO = new HeadHuntPostDTO(); // 새로운 HeadHuntPostDTO 객체 생성
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 저장
		headHuntPostDTO.setPostCategory(rs.getString("POST_category"));            		// 게시글 카테고리
		headHuntPostDTO.setHeadHuntPostId(rs.getString("HEADHUNTPOST_id")); 			// 구인글 아이디
		headHuntPostDTO.setMemberId(rs.getString("MEMBER_id"));							// 회원 아이디
		headHuntPostDTO.setMemberNickname(rs.getString("MEMBER_nickname"));				// 회원 닉네임
		headHuntPostDTO.setHeadHuntPostTitle(rs.getString("HEADHUNTPOST_title")); 		// 구인글 제목
		headHuntPostDTO.setHeadHuntPostContent(rs.getString("HEADHUNTPOST_content")); 	// 구인글 내용
		headHuntPostDTO.setHeadHuntPostDate(rs.getString("HEADHUNTPOST_date")); 		// 구인글 작성일
		headHuntPostDTO.setHeadHuntPostViewcnt(rs.getString("HEADHUNTPOST_viewcnt")); 	// 구인글 조회수
		headHuntPostDTO.setRecommendCnt(rs.getString("RECOMMEND_cnt")); 				// 게시글의 좋아요 수
		headHuntPostDTO.setPostImgName(rs.getString("POSTIMG_name"));					// 게시글 대표 이미지
		return headHuntPostDTO; // headHuntPostDTO에 저장된 데이터들을 반환
	}
}

// 회원 페이지 - 특정 회원이 작성한 구인글 목록 출력 RowMapper 클래스.전미지
class HeadHuntPostMemberRowMapper implements RowMapper<HeadHuntPostDTO> {
	@Override // mapRow 메서드 오버라이드
	public HeadHuntPostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 매핑(저장)하는 메서드

		HeadHuntPostDTO headHuntPostDTO = new HeadHuntPostDTO(); // 새로운 HeadHuntPostDTO 객체 생성
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 저장
		headHuntPostDTO.setPostCategory(rs.getString("POST_category"));            		// 게시글 카테고리
		headHuntPostDTO.setHeadHuntPostId(rs.getString("HEADHUNTPOST_id")); 			// 구인글 아이디
		headHuntPostDTO.setMemberNickname(rs.getString("MEMBER_nickname"));				// 회원 닉네임
		headHuntPostDTO.setHeadHuntPostTitle(rs.getString("HEADHUNTPOST_title")); 		// 구인글 제목
		headHuntPostDTO.setHeadHuntPostContent(rs.getString("HEADHUNTPOST_content")); 	// 구인글 내용
		headHuntPostDTO.setHeadHuntPostDate(rs.getString("HEADHUNTPOST_date")); 		// 구인글 작성일
		headHuntPostDTO.setHeadHuntPostViewcnt(rs.getString("HEADHUNTPOST_viewcnt")); 	// 구인글 조회수
		headHuntPostDTO.setRecommendCnt(rs.getString("RECOMMEND_cnt")); 				// 게시글의 좋아요 수
		headHuntPostDTO.setPostImgName(rs.getString("POSTIMG_name"));					// 게시글 대표 이미지
		return headHuntPostDTO; // headHuntPostDTO에 저장된 데이터들을 반환
	}
}

// ---------------------------------------------------------------------- SELECTONE ----------------------------------------------------------------------

// 게시글 이미지 저장 시 필요한 게시글 아이디의 최대값을 저장할 RowMapper 클래스.전미지
class SelectOneMaxPostIdRowMapper implements RowMapper<HeadHuntPostDTO> {
	@Override
	public HeadHuntPostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 매핑(저장)하는 메서드
		
		HeadHuntPostDTO headHuntPostDTO = new HeadHuntPostDTO(); // 새로운 HeadHuntPostDTO 객체 생성
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 저장	
		System.out.println("HeadHuntPostDAO (SelectOneMaxPostIdRowMapper) In로그 = [" + headHuntPostDTO + "]");
		headHuntPostDTO.setHeadHuntPostId(rs.getString("MAX(HEADHUNTPOST_id)")); // 제일 최근 추가된 게시글 아이디
		System.out.println("HeadHuntPostDAO (SelectOneMaxPostIdRowMapper) Out로그 = [" + headHuntPostDTO + "]");
		return headHuntPostDTO; // headHuntPostDTO에 저장된 데이터들을 반환	
	}
}

// 메인 페이지 - 최신 게시글 목록 출력 RowMapper 클래스.전미지
class SelectOneHeadHuntPostRecentRowMapper implements RowMapper<HeadHuntPostDTO> {
	@Override // mapRow 메서드 오버라이드
	public HeadHuntPostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 매핑(저장)하는 메서드

		HeadHuntPostDTO headHuntPostDTO = new HeadHuntPostDTO(); // 새로운 HeadHuntPostDTO 객체 생성
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 저장
		headHuntPostDTO.setPostCategory(rs.getString("POST_category"));            		// 게시글 카테고리
		headHuntPostDTO.setHeadHuntPostId(rs.getString("HEADHUNTPOST_id")); 			// 구인글 아이디
		headHuntPostDTO.setHeadHuntPostTitle(rs.getString("HEADHUNTPOST_title")); 		// 구인글 제목
		headHuntPostDTO.setHeadHuntPostContent(rs.getString("HEADHUNTPOST_content")); 	// 구인글 내용
		headHuntPostDTO.setHeadHuntPostDate(rs.getString("HEADHUNTPOST_date")); 		// 구인글 작성일
		headHuntPostDTO.setRecommendCnt(rs.getString("RECOMMEND_cnt")); 				// 게시글의 좋아요 수
		headHuntPostDTO.setPostImgName(rs.getString("POSTIMG_name"));					// 게시글 대표 이미지
		return headHuntPostDTO; // headHuntPostDTO에 저장된 데이터들을 반환
	}
}

// 구인글 페이지 - 구인글 상세 출력 시 필요한 데이터를 저장할 RowMapper 클래스.전미지
class SelectOneHeadHuntPostRowMapper implements RowMapper<HeadHuntPostDTO> {
	@Override // mapRow 메서드 오버라이드
	public HeadHuntPostDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 매핑(저장)하는 메서드

		HeadHuntPostDTO headHuntPostDTO = new HeadHuntPostDTO(); // 새로운 HeadHuntPostDTO 객체 생성
		// ResultSet에 저장된 데이터를 HeadHuntPostDTO 객체에 저장
		headHuntPostDTO.setPostCategory(rs.getString("POST_category"));            		// 게시글 카테고리
		headHuntPostDTO.setHeadHuntPostId(rs.getString("HEADHUNTPOST_id")); 			// 구인글 아이디
		headHuntPostDTO.setMemberId(rs.getString("MEMBER_id")); 						// 회원 아이디
		headHuntPostDTO.setMemberNickname(rs.getString("MEMBER_nickname")); 			// 회원 닉네임
		headHuntPostDTO.setMemberIntroduction(rs.getString("MEMBER_introduction"));		// 회원 자기소개
		headHuntPostDTO.setProfileImgName(rs.getString("PROFILEIMG_name")); 			// 회원 프로필
		headHuntPostDTO.setHeadHuntPostTitle(rs.getString("HEADHUNTPOST_title")); 		// 구인글 제목
		headHuntPostDTO.setHeadHuntPostContent(rs.getString("HEADHUNTPOST_content")); 	// 구인글 내용
		headHuntPostDTO.setHeadHuntPostRole(rs.getString("HEADHUNTPOST_role")); 		// 구인글 직업 ( 모델 / 사진작가 )
		headHuntPostDTO.setHeadHuntPostRegion(rs.getString("HEADHUNTPOST_region")); 	// 구인글 작업 지역
		headHuntPostDTO.setHeadHuntPostPay(rs.getInt("HEADHUNTPOST_pay")); 				// 구인글 작업 페이
		headHuntPostDTO.setHeadHuntPostWorkDate(rs.getString("HEADHUNTPOST_workDate")); // 구인글 작업 날짜
		headHuntPostDTO.setHeadHuntPostConcept(rs.getString("HEADHUNTPOST_concept")); 	// 구인글 촬영 컨셉
		headHuntPostDTO.setHeadHuntPostDate(rs.getString("HEADHUNTPOST_date")); 		// 구인글 작성일
		headHuntPostDTO.setHeadHuntPostViewcnt(rs.getString("HEADHUNTPOST_viewcnt")); 	// 구인글 조회수
		headHuntPostDTO.setRecommendCnt(rs.getString("RECOMMEND_cnt")); 				// 게시글의 좋아요 수
		return headHuntPostDTO; // headHuntPostDTO에 저장된 데이터들을 반환
	}
}
