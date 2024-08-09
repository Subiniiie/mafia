import React, { useState, useEffect, useParams } from "react";
import { Link } from "react-router-dom";
import axios from "axios";
import styles from "./GamePageHeader.module.css"
import GameSettingsModal from "../../modals/GameSettingsModal";

function GamePageHeader({ gameData }) {
    const roomTitle = gameData.title
    console.log('유저 목록이야', gameData.userList)
    const roomManagerCheck = gameData.userList.find(user => user.owner === true)
    const roomManager = roomManagerCheck.userId
    console.log('너 방장이야?', roomManagerCheck)
    console.log('너가 방장이구나', roomManager)
    const roomManagerSettings = <button className={styles.settings} onClick={openModal}>게임설정</button>
    const roomId = gameData.roomId
    
    
    // 방장만 게임 설정 바꿀 수 있게
    // 버튼을 클릭하면 게임 설정 모달이 열림
    const [ isModalOpen, setIsModalOpen ] = useState(false)
    const [ blackBackground, setBlackBackground ] = useState(false)
    
    function openModal() {
        setIsModalOpen(!isModalOpen)
        setBlackBackground((preState) => !preState)
    }
    return (
        <>
            <div className={styles.container}>
                <div className={styles.header}>
                    <div className={styles.roomTitle}>
                        {roomTitle}
                    </div>
                    <div className={styles.right}>
                        {roomManager ? roomManagerSettings : null}
                            <Link to="/game-list" className={styles.exit}>
                                <img src="/exit.png" alt="exit.png" className={styles.exitImage}/>
                                나가기
                            </Link>
                    </div>
                </div>
                <div>
                    {isModalOpen ? <GameSettingsModal isOpen={isModalOpen} openModal={openModal} roomId={roomId} /> : null}
                </div>
            </div>
            { blackBackground ? <div className={styles.black} onClick={openModal}></div> : null}
        </>
    )
}

export default GamePageHeader;