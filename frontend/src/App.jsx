import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import DashboardLayout from './layouts/DashboardLayout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import DocumentListPage from './pages/DocumentListPage';
import PlaceholderPage from './pages/PlaceholderPage';
import GroupsPage from './pages/GroupsPage';
import GradesPage from './pages/GradesPage';
import StudentGradesPage from './pages/StudentGradesPage';
import ReviewsPage from './pages/ReviewsPage';
import DeadlineManagementPage from './pages/DeadlineManagementPage';
import NotificationsPage from './pages/NotificationsPage';
import PrivateRoute from './components/PrivateRoute';
import { useAuth } from './context/AuthContext';

// Router component to decide which grades page to show
const GradesPageRouter = () => {
  const { user } = useAuth();
  const roleName = user?.role?.name;

  // Students see their grades, Committee/FYP Committee see grading interface
  if (roleName === 'STUDENT') {
    return <StudentGradesPage />;
  }
  return <GradesPage />;
};

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route element={<PrivateRoute><DashboardLayout /></PrivateRoute>}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/documents" element={<DocumentListPage />} />
            <Route path="/groups" element={
              <PrivateRoute roles={[4]}>
                <GroupsPage />
              </PrivateRoute>
            } />
            <Route path="/grades" element={
              <PrivateRoute roles={[1, 3, 4]}>
                <GradesPageRouter />
              </PrivateRoute>
            } />
            <Route path="/reviews" element={
              <PrivateRoute roles={[2]}>
                <ReviewsPage />
              </PrivateRoute>
            } />
            <Route path="/deadlines" element={
              <PrivateRoute roles={[4]}>
                <DeadlineManagementPage />
              </PrivateRoute>
            } />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
          </Route>
        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;
