import {PropsWithChildren} from "react";
import {Link, useLocation} from "react-router-dom";
import {
    Box,
    Drawer,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Toolbar,
    AppBar,
    Typography
} from "@mui/material";
import {
    CalendarMonth,
    Person,
    School,
    MeetingRoom
} from "@mui/icons-material";

const drawerWidth = 240

const menuItems = [
    { text: "Master Schedule", path: "/", icon: <CalendarMonth /> },
    { text: "Teacher Schedules", path: "/teacher", icon: <Person /> },
    { text: "Student Schedules", path: "/student", icon: <School /> },
    { text: "Classroom Schedules", path: "/classroom", icon: <MeetingRoom /> }
]

export default ({children}: PropsWithChildren) => {
    const location = useLocation()

    return (
        <Box sx={{ display: 'flex' }}>
            <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
                <Toolbar>
                    <Typography variant="h6" noWrap component="div">
                        Maplewood High School - Scheduling System
                    </Typography>
                </Toolbar>
            </AppBar>
            <Drawer
                variant="permanent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: drawerWidth,
                        boxSizing: 'border-box'
                    }
                }}
            >
                <Toolbar />
                <Box sx={{ overflow: 'auto' }}>
                    <List>
                        {menuItems.map((item) => (
                            <ListItem key={item.path} disablePadding>
                                <ListItemButton
                                    component={Link}
                                    to={item.path}
                                    selected={location.pathname === item.path}
                                >
                                    <ListItemIcon>
                                        {item.icon}
                                    </ListItemIcon>
                                    <ListItemText primary={item.text} />
                                </ListItemButton>
                            </ListItem>
                        ))}
                    </List>
                </Box>
            </Drawer>
            <Box component="main" sx={{ flexGrow: 1, p: 0 }}>
                <Toolbar />
                {children}
            </Box>
        </Box>
    )
}
