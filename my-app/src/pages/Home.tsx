import React, { useState } from "react";
import "./Home.css";

type Study = {
    id: number;
    name: string;
    role: string;
};

type Assignment = {
    id: number;
    title: string;
    due: string;
    status: "완료" | "진행중" | "미제출";
};

type Todo = {
    id: number;
    text: string;
    checked: boolean;
};

const Home: React.FC = () => {
    const [sidebarOpen, setSidebarOpen] = useState(true);

    const studies: Study[] = [
        { id: 1, name: "알고리즘 스터디", role: "스터디원" },
        { id: 2, name: "AI 프로젝트 스터디", role: "스터디장" },
        { id: 3, name: "CS 면접 대비", role: "스터디원" },
        { id: 4, name: "웹 개발 협업 스터디", role: "스터디원" },
    ];

    const assignments: Assignment[] = [
        { id: 1, title: "자료구조 문제 풀이 10문제", due: "오늘 마감", status: "진행중" },
        { id: 2, title: "ERD 설계 초안 제출", due: "내일 마감", status: "미제출" },
        { id: 3, title: "React 컴포넌트 분리 과제", due: "제출 완료", status: "완료" },
    ];

    const todos: Todo[] = [
        { id: 1, text: "AI 퀴즈 1회차 풀기", checked: true },
        { id: 2, text: "팀 프로젝트 회의록 확인", checked: true },
        { id: 3, text: "백엔드 API 명세 읽기", checked: false },
        { id: 4, text: "웹소켓 채팅 구조 정리", checked: false },
    ];

    const notices = [
        "금요일 오후 7시 정기 회의 진행",
        "이번 주 공동 프로젝트 중간 점검 제출",
        "AI 자동 퀴즈 기능 시범 운영 예정",
    ];

    const schedules = [
        { title: "스터디 주간 회의", time: "오늘 19:00" },
        { title: "과제 마감", time: "내일 23:59" },
        { title: "프로젝트 발표 준비", time: "금요일 18:00" },
    ];

    return (
        <div className="home">
            <aside className={`sidebar ${sidebarOpen ? "open" : "closed"}`}>
                <div className="sidebar-top">
                    <div className="sidebar-header">
                        {sidebarOpen && <h2 className="workspace-title">StudySpace</h2>}
                        <button
                            className="toggle-btn"
                            onClick={() => setSidebarOpen(!sidebarOpen)}
                        >
                            {sidebarOpen ? "◀" : "▶"}
                        </button>
                    </div>

                    {sidebarOpen && (
                        <div className="sidebar-user-card">
                            <div className="user-avatar">김</div>
                            <div>
                                <p className="user-name">김현수</p>
                                <span className="user-role">AI Study Member</span>
                            </div>
                        </div>
                    )}
                </div>

                <div className="sidebar-section">
                    {sidebarOpen && <p className="section-label">내 스터디</p>}
                    <div className="study-list">
                        {studies.map((study) => (
                            <button key={study.id} className="study-item">
                                <span className="study-icon">📘</span>
                                {sidebarOpen && (
                                    <span className="study-meta">
                                        <span className="study-name">{study.name}</span>
                                        <span className="study-role">{study.role}</span>
                                    </span>
                                )}
                            </button>
                        ))}
                    </div>
                </div>

                <div className="sidebar-section sidebar-actions">
                    <button className="action-btn primary">
                        <span>＋</span>
                        {sidebarOpen && <span>스터디 생성</span>}
                    </button>
                    <button className="action-btn secondary">
                        <span>↗</span>
                        {sidebarOpen && <span>스터디 참여</span>}
                    </button>
                </div>

                <div className="sidebar-bottom">
                    <button className="bottom-link">
                        <span>🏠</span>
                        {sidebarOpen && <span>홈</span>}
                    </button>
                    <button className="bottom-link">
                        <span>⚙</span>
                        {sidebarOpen && <span>설정</span>}
                    </button>
                </div>
            </aside>

            <main className="main-content">
                <header className="topbar">
                    <div>
                        <p className="topbar-path">내 스터디 / AI 프로젝트 스터디</p>
                        <h1>AI 프로젝트 스터디</h1>
                    </div>

                    <div className="topbar-actions">
                        <button className="top-btn">공유</button>
                        <button className="top-btn primary">새 과제</button>
                    </div>
                </header>

                <section className="hero-card">
                    <div className="hero-left">
                        <span className="hero-badge">스터디장 중심 운영</span>
                        <h2>과제, 일정, 프로젝트 현황을 한 번에 관리하세요</h2>
                        <p>
                            AI 자동 퀴즈, 과제 분석, 팀 프로젝트 진행률 확인까지.
                            스터디 운영에 필요한 정보를 한 화면에서 빠르게 확인할 수 있습니다.
                        </p>

                        <div className="hero-summary">
                            <div className="summary-box">
                                <h3>12명</h3>
                                <p>참여 인원</p>
                            </div>
                            <div className="summary-box">
                                <h3>3개</h3>
                                <p>진행중 과제</p>
                            </div>
                            <div className="summary-box">
                                <h3>78%</h3>
                                <p>프로젝트 진척도</p>
                            </div>
                        </div>
                    </div>

                    <div className="hero-right">
                        <div className="progress-card">
                            <p className="progress-title">이번 주 프로젝트 진행률</p>
                            <div className="progress-bar">
                                <div className="progress-fill" style={{ width: "78%" }}></div>
                            </div>
                            <strong>78%</strong>
                        </div>
                    </div>
                </section>

                <section className="content-grid">
                    <div className="content-card large">
                        <div className="card-title-row">
                            <h3>진행중 과제</h3>
                            <button>전체보기</button>
                        </div>

                        <div className="assignment-list">
                            {assignments.map((assignment) => (
                                <div className="assignment-item" key={assignment.id}>
                                    <div>
                                        <p className="assignment-title">{assignment.title}</p>
                                        <span className="assignment-due">{assignment.due}</span>
                                    </div>
                                    <span className={`status-badge ${assignment.status}`}>
                                        {assignment.status}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="content-card">
                        <div className="card-title-row">
                            <h3>TO DO</h3>
                        </div>

                        <div className="todo-list">
                            {todos.map((todo) => (
                                <label className="todo-item" key={todo.id}>
                                    <input type="checkbox" checked={todo.checked} readOnly />
                                    <span className={todo.checked ? "checked" : ""}>{todo.text}</span>
                                </label>
                            ))}
                        </div>
                    </div>

                    <div className="content-card">
                        <div className="card-title-row">
                            <h3>공지사항</h3>
                        </div>

                        <ul className="notice-list">
                            {notices.map((notice, index) => (
                                <li key={index}>{notice}</li>
                            ))}
                        </ul>
                    </div>

                    <div className="content-card">
                        <div className="card-title-row">
                            <h3>다가오는 일정</h3>
                        </div>

                        <div className="schedule-list">
                            {schedules.map((schedule, index) => (
                                <div className="schedule-item" key={index}>
                                    <p>{schedule.title}</p>
                                    <span>{schedule.time}</span>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="content-card wide">
                        <div className="card-title-row">
                            <h3>스터디 활동 요약</h3>
                        </div>

                        <div className="activity-grid">
                            <div className="activity-box">
                                <strong>24</strong>
                                <span>이번 주 생성된 AI 퀴즈</span>
                            </div>
                            <div className="activity-box">
                                <strong>18</strong>
                                <span>제출된 과제 수</span>
                            </div>
                            <div className="activity-box">
                                <strong>9</strong>
                                <span>실시간 채팅 참여자</span>
                            </div>
                            <div className="activity-box">
                                <strong>4</strong>
                                <span>예정된 일정</span>
                            </div>
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
};

export default Home;