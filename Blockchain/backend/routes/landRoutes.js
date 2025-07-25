import express from 'express';
import {
  getLandDetails,
  listAllLands,
  uploadLandDocuments,
  getOwnershipAndDisputeHistory,
  initiateTransfer,
  confirmTransfer,
  getTransferStatus,
  flagDispute,
  listDocumentsForParcel,
  uploadMiddleware,
} from "../controllers/landController.js";

const router = express.Router();

// 1. Land Management
router.get("/:surveyNumber", getLandDetails);
router.get("/lands", listAllLands);
router.post("/:surveyNumber/documents", uploadMiddleware, uploadLandDocuments);
router.get("/:surveyNumber/history", getOwnershipAndDisputeHistory);

// 2. Ownership Transfer
router.post("/:surveyNumber/transfer/initiate", initiateTransfer);
router.post("/:surveyNumber/transfer/confirm", confirmTransfer);
router.get("/:surveyNumber/transfer/status", getTransferStatus);

// 3. Dispute Management
router.post("/disputes/flag", flagDispute);

// 4. Document Handling (General)
router.get("/documents/:surveyNumber", listDocumentsForParcel);

export default router;
