import {createRoot} from "react-dom/client";
import {BrowserRouter, Routes, Route} from "react-router-dom";
import Layout from "./section/Layout";
import MasterSchedule from "./section/MasterSchedule";
import TeacherSchedule from "./section/TeacherSchedule";
import StudentSchedule from "./section/StudentSchedule";
import ClassroomSchedule from "./section/ClassroomSchedule";

import "./css/base.scss";

const Root = () => {
    return (
        <BrowserRouter>
            <Layout>
                <Routes>
                    <Route path="/" element={<MasterSchedule />} />
                    <Route path="/teacher" element={<TeacherSchedule />} />
                    <Route path="/student" element={<StudentSchedule />} />
                    <Route path="/classroom" element={<ClassroomSchedule />} />
                </Routes>
            </Layout>
        </BrowserRouter>
    )
}

createRoot(document.getElementById('root')).render(
    <Root />
)
