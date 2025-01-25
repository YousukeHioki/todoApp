import { Routes, Route } from "react-router-dom";
import { Provider } from "jotai";
import App from "./App.tsx";
import { TodoApp } from "./pages/TodoApp.tsx"; //Provider内で状態を共有する場合に利用

function AppRoutes() {
    return (
        <Provider>
            <Routes>
                <Route path="/sample" element={<App />} />
                <Route path="/" element={<TodoApp />} />
            </Routes>
        </Provider>
    );
}

export default AppRoutes;
