import { useState } from "react";
import type { KeyboardEvent } from "react";
import { useNavigate } from "react-router-dom";
import "./Login.css";

function Login() {
    const [name, setName] = useState("");
    const navigate = useNavigate();

    const handleEnter = () => {
        if (!name.trim()) {
            alert("이름을 입력하세요.");
            return;
        }

        navigate("/home");
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            handleEnter();
        }
    };

    return (
        <div className="login-page">
            <div className="login-card">
                <p className="login-label">Enter your name</p>

                <h1 className="login-title">SwitMe</h1>

                <input
                    type="text"
                    placeholder="이름을 입력하세요"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="login-input"
                />

                <button className="login-button" onClick={handleEnter}>
                    Continue
                </button>
            </div>
        </div>
    );
}

export default Login;